package chococraft.entity;

import net.minecraft.util.RandomSource;

import static chococraft.entity.ChocoboColor.*;

/**
 * getBabyAnimalColor of the nine EntityChocobo&lt;Colour&gt; classes, the colour of the chicobo of {@code self} and
 * {@code other}. Copied branch by branch, with the fall-throughs and overwritten results of the original (yellow with
 * white, black with white): the table is what the mod did, not what its Chocopedia says. {@code bothFedGold}: both
 * parents were made to love with a golden gysahl.
 */
public final class ChocoboBreeding {
    private ChocoboBreeding() {
    }

    public static ChocoboColor babyColor(ChocoboColor self, ChocoboColor other, boolean bothFedGold, RandomSource random) {
        if (self == PURPLE) {
            return switch (other) {
                case YELLOW, GREEN, BLUE, WHITE, BLACK -> other;
                default -> PURPLE;
            };
        }
        int r = random.nextInt(100);
        return switch (self) {
            case YELLOW -> yellow(other, bothFedGold, r);
            case GREEN -> green(other, bothFedGold, r);
            case BLUE -> blue(other, bothFedGold, r);
            case WHITE -> white(other, bothFedGold, r);
            case BLACK -> black(other, bothFedGold, r);
            default -> golden(other, bothFedGold, r);
        };
    }

    private static ChocoboColor yellow(ChocoboColor other, boolean gold, int r) {
        ChocoboColor c = YELLOW;
        switch (other) {
            case YELLOW -> {
                if (r > 88) {
                    c = BLUE;
                } else if (r > 75) {
                    c = gold ? BLUE : GREEN;
                } else if (r > 50 && gold) {
                    c = GREEN;
                }
            }
            case GREEN, BLUE, BLACK -> {
                if (r > 50) {
                    c = other;
                }
            }
            case WHITE -> {
                if (r > 75) {
                    c = BLACK;
                }
                if (r > 50) {
                    c = gold ? BLACK : WHITE;
                }
                if (r > 38 || r > 25 && gold) {
                    c = WHITE;
                }
            }
            default -> {
            }
        }
        return c;
    }

    private static ChocoboColor green(ChocoboColor other, boolean gold, int r) {
        ChocoboColor c = GREEN;
        switch (other) {
            case YELLOW -> {
                if (r < 50) {
                    c = YELLOW;
                }
            }
            case BLUE -> {
                if (r > 75) {
                    c = WHITE;
                } else if (r > 50) {
                    c = gold ? WHITE : BLUE;
                } else if (r > 38 || r > 25 && gold) {
                    c = BLUE;
                }
            }
            case BLACK, WHITE -> {
                if (r <= 75 && (r <= 50 || !gold)) {
                    if (r > 25 && !gold || r < 25) {
                        c = YELLOW;
                    }
                } else {
                    c = other;
                }
            }
            case GOLD, PINK, RED -> {
                if (r > 75 && gold) {
                    c = GOLD;
                }
            }
            default -> {
            }
        }
        return c;
    }

    private static ChocoboColor blue(ChocoboColor other, boolean gold, int r) {
        ChocoboColor c = BLUE;
        switch (other) {
            case YELLOW -> {
                if (r < 50) {
                    c = YELLOW;
                }
            }
            case GREEN -> {
                if (r <= 75 && (r <= 50 || !gold)) {
                    if (r > 25 && !gold || r < 25) {
                        c = GREEN;
                    }
                } else {
                    c = WHITE;
                }
            }
            case BLACK, WHITE -> {
                if (r <= 75 && (r <= 50 || !gold)) {
                    if (r > 25 && !gold || r < 25) {
                        c = YELLOW;
                    }
                } else {
                    c = other;
                }
            }
            case GOLD, PINK, RED -> {
                if (r > 75 && gold) {
                    c = GOLD;
                }
            }
            default -> {
            }
        }
        return c;
    }

    private static ChocoboColor white(ChocoboColor other, boolean gold, int r) {
        ChocoboColor c = WHITE;
        switch (other) {
            case YELLOW -> {
                if (r <= 75 && (r <= 50 || !gold)) {
                    if (r > 25 && !gold || r < 25) {
                        c = YELLOW;
                    }
                } else {
                    c = BLACK;
                }
            }
            case GREEN, BLUE -> {
                if (r < 75) {
                    if ((r <= 50 || gold) && r <= 38) {
                        c = r > 25 ? (gold ? other : YELLOW) : YELLOW;
                    } else {
                        c = other;
                    }
                }
            }
            case BLACK -> {
                if (gold) {
                    if (r > 50) {
                        c = GOLD;
                    } else if (r > 25) {
                        c = BLACK;
                    }
                } else if (r > 75) {
                    c = BLACK;
                } else if (r < 25) {
                    c = YELLOW;
                }
            }
            case GOLD, PINK, RED -> {
                if (r > 75 && gold) {
                    c = GOLD;
                }
            }
            default -> {
            }
        }
        return c;
    }

    private static ChocoboColor black(ChocoboColor other, boolean gold, int r) {
        ChocoboColor c = BLACK;
        switch (other) {
            case YELLOW:
                if (r < 50) {
                    c = YELLOW;
                }
                break;
            case GREEN:
            case BLUE:
                if (r < 75) {
                    if (r > 50) {
                        c = gold ? BLACK : other;
                    }
                    if (r > 38) {
                        c = other;
                    } else if (r >= 25) {
                        c = gold ? other : YELLOW;
                    } else {
                        c = YELLOW;
                    }
                }
                break;
            case WHITE:
                if (gold) {
                    if (r > 50) {
                        c = GOLD;
                    } else if (r < 25) {
                        c = WHITE;
                    }
                } else if (r < 50) {
                    c = r > 25 ? WHITE : YELLOW;
                }
                // no break in the original: white falls through to the golden case
            case GOLD:
            case PINK:
            case RED:
                if (r > 75 && gold) {
                    c = GOLD;
                }
                break;
            default:
                break;
        }
        return c;
    }

    /** Gold, pink and red share one table. */
    private static ChocoboColor golden(ChocoboColor other, boolean gold, int r) {
        ChocoboColor c = GOLD;
        switch (other) {
            case YELLOW -> c = YELLOW;
            case GREEN, BLUE, WHITE, BLACK -> {
                if (r < 75 || !gold) {
                    c = other;
                }
            }
            case GOLD, PINK, RED -> {
                if (!gold) {
                    c = YELLOW;
                }
            }
            case PURPLE -> c = PURPLE;
        }
        return c;
    }
}
