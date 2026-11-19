package qcraft.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EntanglementRegistry: the loaded objects of each entanglement frequency and the next unused frequency (saved in
 * {@code <world>/quantum/entanglements.bin}, see {@link Entanglements}).
 */
public final class EntanglementRegistry<T> {
    private int nextUnusedFrequency = 1;
    private final Map<Integer, List<T>> entanglements = new HashMap<>();

    public synchronized void reset() {
        this.nextUnusedFrequency = 1;
        this.entanglements.clear();
    }

    public synchronized int getNextUnusedFrequency() {
        return this.nextUnusedFrequency;
    }

    public synchronized void setNextUnusedFrequency(int frequency) {
        this.nextUnusedFrequency = frequency;
    }

    public synchronized int getUnusedFrequency() {
        return this.nextUnusedFrequency++;
    }

    public synchronized void register(int frequency, T object) {
        List<T> list = this.entanglements.get(frequency);
        if (list == null) {
            list = new ArrayList<>();
            this.entanglements.put(frequency, list);
            if (frequency >= this.nextUnusedFrequency) {
                this.nextUnusedFrequency = frequency + 1;
            }
        }
        list.add(object);
    }

    public synchronized void unregister(int frequency, T object) {
        List<T> list = this.entanglements.get(frequency);
        if (list != null) {
            list.remove(object);
        }
    }

    /** A copy of the objects of a frequency, or null if it was never used. */
    public synchronized List<T> getEntangledObjects(int frequency) {
        List<T> list = this.entanglements.get(frequency);
        return list == null ? null : new ArrayList<>(list);
    }
}
