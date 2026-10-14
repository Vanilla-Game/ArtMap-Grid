package dev.artmapgrid;

/** Immutable settings captured during entity extraction, before drawing begins. */
public record GridStyle(boolean gridEnabled, boolean centerEnabled, boolean sectionsEnabled,
                        int sectionSize, float opacity, float width) { }
