# App Astreintes — squelette Android (Kotlin)

## Ce que fait ce projet
- Détecte automatiquement le décroché / raccroché d'un appel téléphonique et calcule la durée exacte.
- À la fin de l'appel, une **notification** apparaît : en la touchant, un formulaire s'ouvre avec l'heure de début, l'heure de fin, la durée et le statut JOUR/NUIT déjà pré-remplis.
- Le formulaire propose des **menus déroulants** pour le Type d'appel et le Site géographique (modifiables dans `app/src/main/res/values/arrays.xml`), plus les champs Description et Solution.
- Toutes les fiches sont stockées localement (base Room/SQLite) et visibles dans la liste de l'écran principal.
- Bouton **Exporter en CSV** : génère un fichier avec exactement les mêmes colonnes que `Classeur_astreinte.xlsx` (Qui, Type, Site, Date, Heure Début, Heure Fin, Test Nuit, Description, Solution, Appel rejeté, Temps Exact), prêt à être ouvert dans Excel.

## Comment l'ouvrir

### Option A — Générer l'APK automatiquement via GitHub (sans rien installer)
1. Créer un compte GitHub (gratuit) si vous n'en avez pas.
2. Créer un nouveau dépôt (bouton "New repository"), par exemple nommé `astreinte-app`. Le laisser vide (sans README).
3. Sur votre PC, dézipper ce projet, puis dans le dossier `astreinte-app`, exécuter :
   ```
   git init
   git add .
   git commit -m "Premier import"
   git branch -M main
   git remote add origin https://github.com/VOTRE-COMPTE/astreinte-app.git
   git push -u origin main
   ```
4. Sur la page GitHub du dépôt, ouvrir l'onglet **Actions** : une compilation démarre automatiquement (icône orange puis verte, ~2-3 minutes).
5. Une fois verte, cliquer sur le run terminé → section **Artifacts** en bas de page → télécharger `astreinte-app-debug` : c'est un zip contenant `app-debug.apk`.
6. Transférer cet APK sur le téléphone Android (mail, clé USB, lien de partage...) et l'installer (il faudra autoriser "Installer des apps inconnues" dans les réglages du téléphone, une seule fois).

Le fichier `.github/workflows/build-apk.yml` inclus dans le projet est déjà configuré pour cela — rien à modifier.

### Option B — Via Android Studio (si vous changez d'avis)
1. Installer Android Studio (gratuit).
2. "Open" → sélectionner le dossier `astreinte-app`.
3. Laisser Gradle synchroniser (télécharge les dépendances au premier lancement).
4. Brancher un téléphone Android (ou utiliser un émulateur) et lancer avec le bouton ▶.

## Point important : la détection automatique des appels
Depuis Android 10+, Google **interdit sur le Play Store public** l'accès à l'état téléphonique (`READ_PHONE_STATE`) pour la plupart des apps. Cette fonctionnalité reste en revanche pleinement utilisable si l'app est :
- installée manuellement (fichier `.apk`) sur les téléphones du service, **ou**
- déployée en interne via un MDM / une distribution privée (Play Store à diffusion restreinte "Entreprise").

C'est l'usage classique pour une app métier interne comme celle-ci — pas besoin de la publier publiquement.

À la première ouverture, l'app demande la permission "État du téléphone". Si elle est refusée, la détection automatique ne fonctionnera pas mais la saisie manuelle ("+ Nouvelle intervention") reste utilisable.

## Personnalisation rapide
- **Listes déroulantes** : `app/src/main/res/values/arrays.xml`
- **Seuil JOUR/NUIT** : dans `CallStateReceiver.kt`, actuellement 7h–21h = JOUR (à ajuster selon vos horaires d'astreinte)
- **Icône de l'app** : à ajouter dans `res/mipmap-*` (non incluse dans ce squelette)

## Ce qu'il reste à faire pour une v1 "terrain"
- Ajouter la modification/suppression d'une fiche déjà enregistrée depuis la liste.
- Gérer plusieurs agents (champ "Qui" en liste déroulante alimentée par un annuaire).
- Éventuellement : export direct en `.xlsx` (via une librairie comme Apache POI) plutôt que CSV, si vous préférez éviter l'étape d'ouverture/conversion dans Excel.
- Tests sur différents constructeurs (certains, comme Xiaomi/Huawei, imposent des réglages de batterie supplémentaires pour que le `BroadcastReceiver` reste actif en arrière-plan).
