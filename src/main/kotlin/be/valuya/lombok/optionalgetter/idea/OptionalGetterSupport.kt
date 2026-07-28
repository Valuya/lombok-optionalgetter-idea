package be.valuya.lombok.optionalgetter.idea

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiPrimitiveType

/** Shared `@OptionalGetter` recognition, used by both the augment provider and the find-usages handler. */
object OptionalGetterSupport {

    const val OPTIONAL_GETTER_FQN = "lombok.OptionalGetter"
    const val GETTER_FQN = "lombok.Getter"

    // Matches the overlay's default lombok.optionalGetter.suffix.
    const val SUFFIX = "Optional"

    /** True when `@OptionalGetter` generates an accessor for this field. */
    fun appliesTo(field: PsiField): Boolean {
        if (field.hasModifierProperty(PsiModifier.STATIC)) return false
        // Primitives are rejected by @OptionalGetter, so no accessor is generated for them.
        if (field.type is PsiPrimitiveType) return false
        if (AnnotationUtil.isAnnotated(field, OPTIONAL_GETTER_FQN, 0)) return true
        val containingClass = field.containingClass ?: return false
        return AnnotationUtil.isAnnotated(containingClass, OPTIONAL_GETTER_FQN, 0) &&
            !AnnotationUtil.isAnnotated(field, GETTER_FQN, 0)
    }

    fun getterName(field: PsiField): String = "get" + StringUtil.capitalize(field.name) + SUFFIX

    /** The synthetic accessor for this field (provided by the augment provider), if present. */
    fun findGenerated(field: PsiField): PsiMethod? =
        field.containingClass?.findMethodsByName(getterName(field), false)?.firstOrNull()
}
