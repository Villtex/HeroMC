package tconstruct.items;

import tconstruct.tools.PartType;

/** {@code library.util.IToolPart}: an item the tool builder takes as a part, with its part type and material id. */
public interface IToolPart {
    PartType partType();

    /** The material id (TMaterial.id; for bowstring and fletching the id of their own material). */
    int materialId();
}
