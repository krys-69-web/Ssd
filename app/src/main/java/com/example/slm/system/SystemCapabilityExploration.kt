package com.example.slm.system

/**
 * Analyse approfondie des limitations techniques réelles d'Android 16 sans root.
 * Fournit une documentation technique rigoureuse, transparente et sans tromperie.
 */
object SystemCapabilityExploration {

    data class ConceptDefinition(
        val term: String,
        val definition: String,
        val technicalRole: String
    )

    val CONCEPTS = listOf(
        ConceptDefinition(
            term = "DPI (Dots Per Inch)",
            definition = "Densité logique déclarée au système (densityDpi). Modifie la taille des éléments d'interface UI (sp/dp).",
            technicalRole = "Ne modifie NI la résolution de la dalle physique, NI la taille des buffers de rendu 3D des jeux."
        ),
        ConceptDefinition(
            term = "Résolution Native",
            definition = "Nombre physique réel de sous-pixels adressables de la dalle d'affichage (ex: 1080×2460 ou 1440×3120).",
            technicalRole = "Fixée matériellement par le contrôleur d'affichage (Display Controller / DSI)."
        ),
        ConceptDefinition(
            term = "Résolution de Rendu",
            definition = "Dimensions du Framebuffer ou de la texture off-screen allouée en mémoire GPU dans laquelle le moteur dessine la scène.",
            technicalRole = "Exemple : Rendu calculé en 720×2460 pour économiser 33% de puissance GPU."
        ),
        ConceptDefinition(
            term = "Scaling (Mise à l'échelle)",
            definition = "Transformation matricielle convertissant un buffer de dimensions A vers une surface de dimensions B.",
            technicalRole = "Peut être uniforme (respecte le ratio) ou anamorphique (étirement)."
        ),
        ConceptDefinition(
            term = "Zoom / Crop",
            definition = "Recadrage ou agrandissement partiel du champ de vision (FOV) d'une caméra ou d'une image.",
            technicalRole = "Perd des pixels sur les bords. Ce n'est PAS un étirement."
        ),
        ConceptDefinition(
            term = "Stretch (Étirement)",
            definition = "Modification asymétrique du ratio d'aspect horizontal (X) ou vertical (Y) sans perte de champ de vision.",
            technicalRole = "Élargit les cibles à l'écran (ex: X1.50) tout en préservant l'intégralité de la scène."
        ),
        ConceptDefinition(
            term = "Upscaling IA (Super-Résolution)",
            definition = "Reconstruction directionnelle et synthèse des détails haute fréquence par réseau neuronal ou tenseur spatial.",
            technicalRole = "Remplace le flou bilinéaire par une reconstruction nette des contours et des textures."
        )
    )

    data class GameCategoryAnalysis(
        val category: String,
        val title: String,
        val possibility: String,
        val technicalExplanation: String
    )

    val GAME_CATEGORIES = listOf(
        GameCategoryAnalysis(
            category = "Catégorie A",
            title = "Jeux contrôlés par SLM / Moteur intégré",
            possibility = "100% Fonctionnel",
            technicalExplanation = "SLM Stretch contrôle directement la résolution du buffer de rendu EGL/Vulkan, le shader de reconstruction IA et le pipeline de composition vers l'écran natif."
        ),
        GameCategoryAnalysis(
            category = "Catégorie B",
            title = "Jeux avec API / SDK SLM intégrée",
            possibility = "Fonctionnel avec collaboration",
            technicalExplanation = "Le développeur du jeu intègre la bibliothèque SLM (ou hook Vulkan) pour exposer les paramètres de render scale et de stretch anamorphique directement au moteur de jeu."
        ),
        GameCategoryAnalysis(
            category = "Catégorie C",
            title = "Jeux externes sans intégration (Mode Overlay / MediaProjection)",
            possibility = "Capture & Traitement avec compromis",
            technicalExplanation = "Une application tierce normale sur Android 16 ne peut pas injecter de code dans le processus du jeu en raison du sandboxing SELinux. Le traitement passe par une capture de flux d'affichage ou un overlay HUD. Voir analyse MediaProjection ci-dessous."
        ),
        GameCategoryAnalysis(
            category = "Catégorie D",
            title = "Jeux protégés (DRM / Anti-Cheat / FLAG_SECURE)",
            possibility = "Impossible sans privilèges système",
            technicalExplanation = "Android 16 interdit strictement la capture ou l'interception de surfaces protégées par FLAG_SECURE ou les moteurs anti-cheat au niveau kernel. SLM Stretch refuse explicitement toute fausse promesse."
        )
    )

    data class AndroidArchitectureItem(
        val component: String,
        val role: String,
        val nonRootPermissionVerdict: String
    )

    val ARCHITECTURE_ANALYSIS = listOf(
        AndroidArchitectureItem(
            component = "SurfaceHolder.setFixedSize(w, h)",
            role = "Permet à l'application de demander au système Android d'allouer un buffer de rendu plus petit (ex: 720×2460) qui est ensuite mis à l'échelle matériellement par le Hardware Composer (HWC) jusqu'à la résolution native de l'écran.",
            nonRootPermissionVerdict = "PARFAITEMENT AUTORISÉ : API Android standard sans root pour la propre surface de l'application."
        ),
        AndroidArchitectureItem(
            component = "SurfaceControl & SurfaceFlinger",
            role = "Serveur de composition système qui assemble toutes les couches (windows) des applications actives en mémoire GPU.",
            nonRootPermissionVerdict = "RESTREINT : Une application tierce ne possède de droits de manipulation que sur son propre sous-arbre de couches (SurfaceControl locale). Les couches d'autres applications requièrent la permission ACCESS_SURFACE_FLINGER (réservée au système)."
        ),
        AndroidArchitectureItem(
            component = "MediaProjection & VirtualDisplay",
            role = "API permettant de capturer l'affichage de l'écran après accord explicite de l'utilisateur.",
            nonRootPermissionVerdict = "AUTORISÉ AVEC CONSENTEMENT : Fonctionne sans root mais introduit 16 à 35 ms de latence (buffer copy VRAM -> RAM -> VRAM) et une charge GPU additionnelle qui n'est pas adaptée au jeu compétitif temps réel."
        ),
        AndroidArchitectureItem(
            component = "WindowManager global (wm size / wm density)",
            role = "Commande de modification globale de la résolution d'écran du système.",
            nonRootPermissionVerdict = "INTERDIT AUX APPS CLASSIQUES : Nécessite la permission privileged WRITE_SECURE_SETTINGS accessible uniquement via ADB ou root. SLM Stretch ne prétend pas l'activer sans ADB."
        )
    )
}
