package io.github.cbarlin.aru.core.mirrorhandlers;

/**
 * Different behaviours for merging two instances in a {@link MergedMirror}
 */
public enum MirrorMergingBehaviour {
    /**
     * Values will be taken from the primary source, delegating to the secondary one only if undefined for the primary one
     */
    BASIC,

    /**
     * Boolean values that are true are always preferred, followed by elements from the primary source, then the secondary
     */
    PREFER_TRUE_ELSE_BASIC
}
