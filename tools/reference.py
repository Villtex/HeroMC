#!/usr/bin/env python3
"""Fetch the original Hexxit 1.0.11 (Minecraft 1.5.2) pack and turn it into readable reference sources.

The port in this repository is a rewrite against NeoForge; this script only produces the
*reference* the rewrite is checked against. Nothing it downloads or generates is committed.

  1. downloads every archive listed in pack/hexxit-1.0.11.json from the Technic mirror (md5-verified)
  2. downloads the 1.5.2 MCP mappings (Forge src zip), the vanilla 1.5.2 client jar,
     SpecialSource (remapper) and Vineflower (decompiler)
  3. remaps each mod jar from obfuscated (notch) names to srg names, decompiles it to <out>/<mod>/src
     and renames the srg identifiers to readable MCP names
  4. unpacks its non-class resources to <out>/<mod>/res

Usage:  python3 tools/reference.py [--cache DIR] [--out DIR] [--only name,name] [--jobs N]
        (--only minecraft decompiles Forge-patched Minecraft 1.5.2 itself: the reference for world generation)
Needs:  a JDK (17+) on PATH or JAVA_HOME.
"""
import argparse, csv, hashlib, io, json, os, re, shutil, subprocess, sys, time, urllib.request, zipfile
from concurrent.futures import ThreadPoolExecutor

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
UA = {"User-Agent": "hexxit-port-tools"}
FORGE_SRC = ("https://maven.minecraftforge.net/net/minecraftforge/forge/1.5.2-7.8.1.738/"
             "forge-1.5.2-7.8.1.738-src.zip", None)
MC_CLIENT = ("https://launcher.mojang.com/v1/objects/465378c9dc2f779ae1d6e8046ebc46fb53a57968/client.jar",
             ("sha1", "465378c9dc2f779ae1d6e8046ebc46fb53a57968"))
SPECIALSOURCE = ("https://repo.maven.apache.org/maven2/net/md-5/SpecialSource/1.11.6/"
                 "SpecialSource-1.11.6-shaded.jar", None)
VINEFLOWER = ("https://repo.maven.apache.org/maven2/org/vineflower/vineflower/1.12.0/"
              "vineflower-1.12.0.jar", None)


def digest(path, algo):
    h = hashlib.new(algo)
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def download(url, dst, check=None):
    if os.path.exists(dst) and (check is None or digest(dst, check[0]) == check[1]):
        return dst
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    last = None
    for attempt in range(4):
        try:
            with urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=180) as r:
                data = r.read()
            with open(dst + ".part", "wb") as f:
                f.write(data)
            os.replace(dst + ".part", dst)
            if check is None or digest(dst, check[0]) == check[1]:
                return dst
            last = "checksum mismatch"
        except Exception as e:  # network hiccup: back off and retry
            last = e
        time.sleep(2 ** (attempt + 1))
    raise SystemExit(f"download failed: {url}: {last}")


def java():
    home = os.environ.get("JAVA_HOME")
    return os.path.join(home, "bin", "java") if home else "java"


def build_mappings(forge_src, workdir):
    """MCP 7.51 conf from the Forge src zip: the notch->srg file plus the srg->readable name table."""
    names = {}
    with zipfile.ZipFile(forge_src) as z:
        for name in ("methods.csv", "fields.csv", "params.csv"):
            for row in csv.DictReader(io.TextIOWrapper(z.open(f"forge/fml/conf/{name}"), "utf-8")):
                names[row.get("searge") or row["param"]] = row["name"]
        srg = os.path.join(workdir, "notch-srg.srg")
        with open(srg, "wb") as f:
            f.write(z.read("forge/fml/conf/joined.srg"))
    return srg, names


SRG_TOKEN = re.compile(r"\b(?:func_\d+_[A-Za-z_]+|field_\d+_[A-Za-z_]+|p_\d+_\d+_)\b")


def rename_sources(src, names):
    """srg -> MCP names. Srg identifiers are globally unique, so a token replace is exact (it is what MCP did)."""
    for dirpath, _, files in os.walk(src):
        for fn in files:
            if fn.endswith(".java"):
                path = os.path.join(dirpath, fn)
                text = open(path, encoding="utf-8", errors="replace").read()
                new = SRG_TOKEN.sub(lambda m: names.get(m.group(0), m.group(0)), text)
                if new != text:
                    open(path, "w", encoding="utf-8").write(new)


def inner_archives(outer):
    """(name, bytes) for every jar/zip holding classes inside a Technic archive (or the archive itself)."""
    found = []
    with zipfile.ZipFile(outer) as z:
        for n in z.namelist():
            if n.lower().endswith((".jar", ".zip")):
                data = z.read(n)
                try:
                    with zipfile.ZipFile(io.BytesIO(data)) as inner:
                        if any(x.endswith(".class") for x in inner.namelist()):
                            found.append((os.path.basename(n), data))
                except zipfile.BadZipFile:
                    pass
    return found


def unpack_resources(zf, dest):
    base = os.path.realpath(dest)
    for info in zf.infolist():
        if info.is_dir() or info.filename.endswith(".class"):
            continue
        target = os.path.realpath(os.path.join(dest, info.filename))
        if not target.startswith(base + os.sep):  # zip-slip guard
            continue
        os.makedirs(os.path.dirname(target), exist_ok=True)
        with zf.open(info) as src, open(target, "wb") as dst:
            shutil.copyfileobj(src, dst)


def process(mod, args, tools, maps):
    name = mod["name"]
    archive = os.path.join(args.cache, "zips", f"{name}-{mod['version']}.zip")
    download(mod["url"], archive, ("md5", mod["md5"]))
    out = os.path.join(args.out, name)
    stamp = os.path.join(out, ".done")
    if os.path.exists(stamp) and open(stamp).read() == mod["md5"]:
        return f"{name}: up to date"
    shutil.rmtree(out, ignore_errors=True)
    os.makedirs(out)
    with zipfile.ZipFile(archive) as z:  # loose files (configs, structure templates)
        unpack_resources(z, os.path.join(out, "pack-files"))
    report = []
    for inner_name, data in inner_archives(archive):
        work = os.path.join(args.cache, "work", name)
        os.makedirs(work, exist_ok=True)
        raw = os.path.join(work, "in.jar")
        with open(raw, "wb") as f:
            f.write(data)
        with zipfile.ZipFile(raw) as z:
            unpack_resources(z, os.path.join(out, "res"))
        remapped = os.path.join(work, "remapped.jar")
        cp = os.pathsep.join([tools["specialsource"], tools["forge_runtime"], tools["mc"]])
        r = subprocess.run([java(), "-cp", cp, "net.md_5.specialsource.SpecialSource", "--live", "-q",
                            "-m", maps[0], "-i", raw, "-o", remapped],
                           capture_output=True, text=True)
        if r.returncode != 0:
            report.append(f"remap FAILED {inner_name}: {r.stderr.strip()[-300:]}")
            continue
        src = os.path.join(out, "src")
        os.makedirs(src, exist_ok=True)
        r = subprocess.run([java(), "-jar", tools["vineflower"], "-log=ERROR", "-nls=1", "-din=1", "-rsy=1",
                            "-dgs=1", remapped, src], capture_output=True, text=True)
        rename_sources(src, maps[1])
        n = sum(len([f for f in fs if f.endswith(".java")]) for _, _, fs in os.walk(src))
        report.append(f"{inner_name}: {n} source files"
                      + ("" if r.returncode == 0 else f" (decompiler exit {r.returncode})"))
    with open(stamp, "w") as f:
        f.write(mod["md5"])
    return f"{name}: " + "; ".join(report or ["data only"])


def minecraft_reference(args, tools, maps):
    """Decompile Minecraft 1.5.2 as the pack ran it (vanilla client jar overlaid with the Forge-patched classes)."""
    out = os.path.join(args.out, "minecraft")
    if os.path.exists(os.path.join(out, ".done")):
        return "minecraft: up to date"
    shutil.rmtree(out, ignore_errors=True)
    work = os.path.join(args.cache, "work", "minecraft")
    os.makedirs(work, exist_ok=True)
    merged = os.path.join(work, "in.jar")
    with zipfile.ZipFile(merged, "w", zipfile.ZIP_DEFLATED) as dst:
        seen = set()
        for jar in (tools["forge_runtime"], tools["mc"]):  # first one wins
            with zipfile.ZipFile(jar) as src:
                for n in src.namelist():
                    if n.endswith(".class") and n not in seen and not n.startswith(("paulscode/", "ibxm/", "com/jcraft/")):
                        seen.add(n)
                        dst.writestr(n, src.read(n))
    remapped = os.path.join(work, "remapped.jar")
    cp = os.pathsep.join([tools["specialsource"], tools["forge_runtime"], tools["mc"]])
    r = subprocess.run([java(), "-cp", cp, "net.md_5.specialsource.SpecialSource", "--live", "-q",
                        "-m", maps[0], "-i", merged, "-o", remapped], capture_output=True, text=True)
    if r.returncode != 0:
        return f"minecraft: remap FAILED: {r.stderr.strip()[-300:]}"
    src = os.path.join(out, "src")
    os.makedirs(src)
    subprocess.run([java(), "-jar", tools["vineflower"], "-log=ERROR", "-nls=1", "-din=1", "-rsy=1", "-dgs=1",
                    remapped, src], capture_output=True, text=True)
    rename_sources(src, maps[1])
    n = sum(len([f for f in fs if f.endswith(".java")]) for _, _, fs in os.walk(src))
    open(os.path.join(out, ".done"), "w").write("1")
    return f"minecraft: {n} source files"


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--cache", default=os.path.join(ROOT, ".cache"))
    ap.add_argument("--out", default=os.path.join(ROOT, ".reference"))
    ap.add_argument("--only", default="")
    ap.add_argument("--jobs", type=int, default=max(1, (os.cpu_count() or 2) // 2))
    args = ap.parse_args()
    manifest = json.load(open(os.path.join(ROOT, "pack", "hexxit-1.0.11.json")))
    mods = {m["name"]: m for m in manifest["mods"]}
    wanted = [n for n in args.only.split(",") if n] or sorted(mods)
    unknown = [n for n in wanted if n not in mods and n != "minecraft"]
    if unknown:
        raise SystemExit(f"unknown mod(s): {', '.join(unknown)}")

    t = os.path.join(args.cache, "tools")
    forge_src = download(FORGE_SRC[0], os.path.join(t, "forge-1.5.2-src.zip"), FORGE_SRC[1])
    tools = {
        "mc": download(MC_CLIENT[0], os.path.join(t, "minecraft-1.5.2-client.jar"), MC_CLIENT[1]),
        "specialsource": download(SPECIALSOURCE[0], os.path.join(t, "specialsource.jar"), SPECIALSOURCE[1]),
        "vineflower": download(VINEFLOWER[0], os.path.join(t, "vineflower.jar"), VINEFLOWER[1]),
    }
    # Forge-patched vanilla classes + Forge/FML API: needed so inherited members resolve while remapping.
    base = mods["basemods"]
    base_zip = download(base["url"], os.path.join(args.cache, "zips", f"basemods-{base['version']}.zip"),
                        ("md5", base["md5"]))
    tools["forge_runtime"] = os.path.join(t, "forge-runtime-1.5.2.jar")
    with zipfile.ZipFile(base_zip) as z, open(tools["forge_runtime"], "wb") as f:
        f.write(z.read("bin/modpack.jar"))
    maps = build_mappings(forge_src, t)

    if "minecraft" in wanted:  # pseudo entry: vanilla 1.5.2 with the Forge patches applied on top
        wanted.remove("minecraft")
        print(minecraft_reference(args, tools, maps), flush=True)

    with ThreadPoolExecutor(max_workers=args.jobs) as pool:
        for line in pool.map(lambda n: process(mods[n], args, tools, maps), wanted):
            print(line, flush=True)


if __name__ == "__main__":
    main()
