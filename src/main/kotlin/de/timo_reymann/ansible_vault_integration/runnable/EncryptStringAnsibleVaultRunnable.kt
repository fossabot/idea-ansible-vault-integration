package de.timo_reymann.ansible_vault_integration.runnable

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import de.timo_reymann.ansible_vault_integration.config.VaultIdentity
import de.timo_reymann.ansible_vault_integration.execution.action.AnsibleVaultEncryptAction
import org.jetbrains.yaml.YAMLElementGenerator

class EncryptStringAnsibleVaultRunnable(
    private val project: Project,
    private val containingFile: PsiFile,
    private val content: String,
    private val element: PsiElement,
    private val vaultIdentity: VaultIdentity? = null
) : AnsibleVaultRunnable {
    @Throws(Exception::class)
    override fun run() {
        val encrypted = AnsibleVaultEncryptAction(project, containingFile, content, vaultIdentity)
            .execute()
        WriteCommandAction.runWriteCommandAction(project) {
            val generatedReplacement =
                YAMLElementGenerator(project).createYamlKeyValue(element.parent.text, encrypted).value

            // Just in case
            if (element.context == null || generatedReplacement == null) {
                return@runWriteCommandAction
            }

            element.context!!.replace(generatedReplacement)
        }
    }

    override val successMessage: String
        get() = "String vaulted and replaced"
}
