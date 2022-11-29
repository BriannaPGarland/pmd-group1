/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.types;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.java.symbols.JMethodSymbol;
import net.sourceforge.pmd.lang.java.symbols.JTypeParameterSymbol;
import net.sourceforge.pmd.lang.java.symbols.SymbolicValue.SymAnnot;

/**
 * The type of a type variable. There are two sorts of those:
 *
 * <ul>
 * <li>Types of type parameters, which have a user-defined name, and an origin.
 * Those may only have an {@linkplain #getUpperBound() upper bound}.
 * <li>Types of {@linkplain #isCaptured() captured variables}, which arise
 * from {@linkplain TypeConversion#capture(JTypeMirror) capture-conversion}. Those can
 * have a non-trivial lower bound.
 * </ul>
 *
 * <p>Type variables may appear in their own bound (F-bound), and we
 * have to make sure all those occurrences are represented by the same instance.
 * We have to pay attention to cycles in our algos too.
 *
 * <p>Type variables do not, in general, use reference identity. Use
 * equals to compare them.
 */
public interface JTypeVar extends JTypeMirror, SubstVar {


    /**
     * Returns the reflected type variable this instance represents,
     * or null if this is a capture variable.
     */
    @Override
    @Nullable JTypeParameterSymbol getSymbol();


    /**
     * Returns the name of this variable, which may something
     * autogenerated if this is a captured variable. This is not necessarily
     * an identifier.
     */
    @NonNull String getName();


    /**
     * Gets the upper bound. This defaults to Object, and may be an
     * {@linkplain JIntersectionType intersection type}.
     *
     * <p>Note that the upper bound of a capture variable is not necessarily
     * the upper bound of the {@linkplain #getCapturedOrigin() captured wildcard}.
     * The declared bound of each variable is {@link TypeSystem#glb(Collection) glb}ed with the declared
     * bound of the wildcard. For example, given {@code class Foo<T extends List<T>>},
     * then {@code Foo<?>} will have {@link TypeSystem#UNBOUNDED_WILD} as a type
     * argument. But the capture of {@code Foo<?>} will look like {@code Foo<capture#.. of ?>},
     * where the capture var's upper bound is actually {@code List<?>}.
     */
    @NonNull JTypeMirror getUpperBound();


    /**
     * Gets the lower bound. {@link TypeSystem#NULL_TYPE} conventionally represents
     * the bottom type (a trivial lower bound).
     */
    @NonNull
    JTypeMirror getLowerBound();


    /**
     * Returns true if this is a capture variable, ie this variable
     * originates from the {@linkplain TypeConversion#capture(JTypeMirror) capture} of a wildcard
     * type argument. Capture variables use reference identity.
     */
    boolean isCaptured();


    /**
     * Returns true if this is a capture variable for the given wildcard.
     */
    boolean isCaptureOf(JWildcardType wildcard);

    /**
     * Returns the original wildcard, if this is a capture variable.
     * Otherwise returns null.
     */
    @Nullable JWildcardType getCapturedOrigin();


    @Override
    default <T, P> T acceptVisitor(JTypeVisitor<T, P> visitor, P p) {
        return visitor.visitTypeVar(this, p);
    }

    /**
     * Like {@link #subst(Function)}, except this typevar is not the
     * subject of the substitution, only its bounds. May return a new
     * tvar, must return this is the bound is unchanged.
     */
    JTypeVar substInBounds(Function<? super SubstVar, ? extends @NonNull JTypeMirror> substitution);

    /**
     * @throws UnsupportedOperationException If this is not a capture var
     */
    JTypeVar cloneWithBounds(JTypeMirror lower, JTypeMirror upper);

    /**
     * Return a new type variable with the same underlying symbol or
     * capture variable, but the upper bound is now the given type.
     *
     * @param newUB New upper bound
     *
     * @return a new tvar
     */
    JTypeVar withUpperBound(@NonNull JTypeMirror newUB);

    @Override // refine return type
    JTypeVar withAnnotations(List<SymAnnot> newTypeAnnots);

    @Override
    default Stream<JMethodSig> streamMethods(Predicate<? super JMethodSymbol> prefilter) {
        // recursively bound type vars will throw this into an infinite cycle
        //  eg <T extends X, X extends T>
        //  this is a compile-time error though
        return getUpperBound().streamMethods(prefilter);
    }


}
