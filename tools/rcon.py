#!/usr/bin/env python3
"""Minimal RCON client for checking a running dev server:  python3 tools/rcon.py [--port 25575] [--password hexxit] CMD...

Each argument is sent as one command and its reply printed. Used for the registry / recipe checks
after a port (e.g. `execute if block 0 0 0 <id>` fails with "Unknown block type" for unregistered ids).
"""
import argparse, socket, struct


def packet(sock, req_id, kind, body):
    data = struct.pack("<ii", req_id, kind) + body.encode("utf-8") + b"\x00\x00"
    sock.sendall(struct.pack("<i", len(data)) + data)
    (size,) = struct.unpack("<i", read(sock, 4))
    payload = read(sock, size)
    return struct.unpack("<i", payload[:4])[0], payload[8:-2].decode("utf-8", "replace")


def read(sock, n):
    buf = b""
    while len(buf) < n:
        chunk = sock.recv(n - len(buf))
        if not chunk:
            raise ConnectionError("server closed the connection")
        buf += chunk
    return buf


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--host", default="127.0.0.1")
    ap.add_argument("--port", type=int, default=25575)
    ap.add_argument("--password", default="hexxit")
    ap.add_argument("commands", nargs="+")
    args = ap.parse_args()
    with socket.create_connection((args.host, args.port), timeout=10) as sock:
        if packet(sock, 1, 3, args.password)[0] == -1:
            raise SystemExit("rcon: wrong password")
        for i, cmd in enumerate(args.commands):
            print(f"> {cmd}\n{packet(sock, 10 + i, 2, cmd)[1]}")


if __name__ == "__main__":
    main()
