package org.logviewer;

import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Service pour gérer la création et l'ouverture de scratch files
 */
@Service(Service.Level.PROJECT)
public final class ScratchFileService {

    private final Project project;

    public ScratchFileService(Project project) {
        this.project = project;
    }

    /**
     * Récupère l'instance du service pour un projet donné
     */
    public static ScratchFileService getInstance(@NotNull Project project) {
        return project.getService(ScratchFileService.class);
    }

    /**
     * Crée et ouvre un scratch file JSON avec le contenu spécifié
     *
     * @param fileName Nom du fichier (sans extension ou avec .json)
     * @param jsonContent Contenu JSON à écrire dans le fichier
     */
    public void createAndOpenJsonScratchFile(@NotNull String fileName, @NotNull String jsonContent) {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                // S'assurer que le nom se termine par .json
                String normalizedFileName = fileName.endsWith(".json") ? fileName : fileName + ".json";

                // Créer le scratch file
                VirtualFile scratchFile = createScratchFile(normalizedFileName, jsonContent);

                if (scratchFile != null) {
                    // Ouvrir le fichier dans l'éditeur
                    FileEditorManager.getInstance(project).openFile(scratchFile, true);
                }
            } catch (Exception e) {
                handleError("Erreur lors de la création du scratch file", e);
            }
        });
    }

    public void createAndOpenTextScratchFile(@NotNull String fileName, @NotNull String jsonContent) {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                // S'assurer que le nom se termine par .json
                String normalizedFileName = fileName.endsWith(".txt") ? fileName : fileName + ".txt";

                // Créer le scratch file
                VirtualFile scratchFile = createScratchFile(normalizedFileName, jsonContent);

                if (scratchFile != null) {
                    // Ouvrir le fichier dans l'éditeur
                    FileEditorManager.getInstance(project).openFile(scratchFile, true);
                }
            } catch (Exception e) {
                handleError("Erreur lors de la création du scratch file", e);
            }
        });
    }

    /**
     * Crée un scratch file sans l'ouvrir
     *
     * @param fileName Nom du fichier
     * @param content Contenu du fichier
     * @return Le VirtualFile créé ou null en cas d'erreur
     */
    public VirtualFile createScratchFile(@NotNull String fileName, @NotNull String content) throws IOException {
        com.intellij.ide.scratch.ScratchFileService scratchFileService = com.intellij.ide.scratch.ScratchFileService.getInstance();

        VirtualFile scratchFile = scratchFileService.findFile(
                ScratchRootType.getInstance(),
                fileName,
                com.intellij.ide.scratch.ScratchFileService.Option.create_if_missing
        );

        if (scratchFile != null) {
            ApplicationManager.getApplication().runWriteAction(() -> {
                try {
                    scratchFile.setBinaryContent(content.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    handleError("Erreur lors de l'écriture du contenu", e);
                }
            });
        }

        return scratchFile;
    }

    /**
     * Crée un scratch file JSON avec un contenu formaté
     *
     * @param fileName Nom du fichier
     * @param jsonContent Contenu JSON (sera formaté si possible)
     * @param openInEditor Si true, ouvre le fichier dans l'éditeur
     */
    public void createFormattedJsonScratchFile(
            @NotNull String fileName,
            @NotNull String jsonContent,
            boolean openInEditor) {

        ApplicationManager.getApplication().invokeLater(() -> {
            String normalizedFileName = fileName.endsWith(".json") ? fileName : fileName + ".json";
            VirtualFile scratchFile = null;
            try {
                scratchFile = createScratchFile(normalizedFileName, jsonContent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (scratchFile != null && openInEditor) {
                FileEditorManager.getInstance(project).openFile(scratchFile, true);
            }
        });
    }

    /**
     * Ouvre un scratch file existant ou le crée s'il n'existe pas
     *
     * @param fileName Nom du fichier
     * @param defaultContent Contenu par défaut si le fichier n'existe pas
     */
    public void openOrCreateScratchFile(@NotNull String fileName, @NotNull String defaultContent) {
        ApplicationManager.getApplication().invokeLater(() -> {
            com.intellij.ide.scratch.ScratchFileService scratchFileService = com.intellij.ide.scratch.ScratchFileService.getInstance();
            String normalizedFileName = fileName.endsWith(".json") ? fileName : fileName + ".json";

            // Chercher d'abord le fichier existant
            VirtualFile scratchFile = null;
            try {
                scratchFile = scratchFileService.findFile(
                        ScratchRootType.getInstance(),
                        normalizedFileName,
                        com.intellij.ide.scratch.ScratchFileService.Option.existing_only
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Si le fichier n'existe pas, le créer avec le contenu par défaut
            if (scratchFile == null) {
                try {
                    scratchFile = createScratchFile(normalizedFileName, defaultContent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            // Ouvrir le fichier
            if (scratchFile != null) {
                FileEditorManager.getInstance(project).openFile(scratchFile, true);
            }
        });
    }

    /**
     * Gestion centralisée des erreurs
     */
    private void handleError(String message, Exception e) {
        // Vous pouvez utiliser un logger ou afficher une notification
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();

        // Optionnel: afficher une notification à l'utilisateur
        // Notifications.Bus.notify(
        //     new Notification("ScratchFileService", "Erreur", message, NotificationType.ERROR),
        //     project
        // );
    }
}