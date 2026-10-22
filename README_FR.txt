NYXIUMORE — compilation automatique, sans Java/Maven sur ton PC

Ce projet n'est PAS le fichier .jar prêt à installer : il faut le compiler dans le cloud.
Le workflow GitHub fourni compile avec Java 25 et dépose le JAR comme artefact.

Ce prototype :
- ajoute 1 à 5 blocs visuels de minerai par chunk lorsqu'un chunk est chargé pour la première fois par le plugin, y compris les chunks déjà explorés ;
- les blocs utilisent l'apparence de minerai de diamant des abîmes ;
- donnent un objet nommé « Minerai de Nyxium » ;
- /nyxium upgrade consomme un Nyxium et améliore l'objet en diamant tenu ; une arme améliorée inflige +3 dégâts.

ATTENTION :
- teste d'abord sur une COPIE du monde. Le plugin place des blocs dans les chunks au chargement.
- ce prototype n'ajoute pas encore de texture personnalisée et l'amélioration se fait avec une commande, pas à la table de forge.
- aucune compilation n'a été exécutée dans cet environnement : le JAR doit être produit par le workflow puis testé avant utilisation en production.

Pour compiler :
1. Crée un dépôt GitHub vide.
2. Décompresse ce ZIP et téléverse le CONTENU du dossier dans le dépôt (le dossier .github doit être inclus).
3. Ouvre l'onglet Actions, choisis « Build NyxiumOre », puis « Run workflow » si nécessaire.
4. Une fois terminé, ouvre l'exécution et télécharge l'artefact « NyxiumOre-JAR ».
5. Décompresse cet artefact : il contient NyxiumOre-1.0.0.jar.
6. Arrête le serveur Falix, fais une sauvegarde, téléverse le .jar dans plugins, puis redémarre.
7. Vérifie /plugins et les logs. N'installe pas sur le monde principal avant le test sur copie.
