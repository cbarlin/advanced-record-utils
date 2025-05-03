/**
 * The package that provides implementations of all the non-core work the processor does. If
 *   you are looking for details about the processor and it's usage, please see https://github.com/cbarlin/advanced-record-utils
 * <p>
 * While the size of this package might seem quite daunting, each individual file should be:
 * <ul>
 *   <li>Self-contained; and</li>
 *   <li>Quite small</li>
 * </ul>
 * <p>
 * This package is sub-divided into the different things that the processor can do, and a "Constants" file. All sub
 *   divisions are {@link NullMarked} meaning that {@code null} use has to be declared.
 * <p>
 * Note that this maven project isn't a module - that's because the `module-info` file would very unweildy given the sheer number
 *   of implementations of services this will provide (85, at time of writing!)
 */
@NullMarked
package io.github.cbarlin.aru.impl;

import org.jspecify.annotations.NullMarked;
