------------------------------------------------ Projet CHESS ------------------------------------------------

Ce projet propose la réalisation d’un jeu d’échecs chinois (Xiangqi) en Java, avec une interface graphique
permettant à l'utilisateur jouer en respectant les règles traditionnelles du Xiangqi.

Réalisé par : Bilel BEN NAYA - Aymen ESSAHLI - Adel MAHI
Formation : L2 Informatique, grp D, CY Cergy Paris Université
Fais le : 27/04/2025

Contexte : -----------------------------------------------------------------------------------------------------

Ce projet a été développé dans le cadre d’un cours de génie logiciel. 
Il a pour objectif de modéliser un jeu complexe en Java, d’implémenter une interface graphique interactive et 
de respecter les spécificités des déplacements propres aux pièces du Xiangqi.

Fonctionnalités : ----------------------------------------------------------------------------------------------

Après le lancement du jeu, le joueur a le choix de jouer en 1v1 contre un autre joueur ou de jouer contre un
bot.
L'application affiche un plateau de Xiangqi de 11x11 cases, incluant la rivière au centre et les palais pour 
chaque joueur. Chaque type de pièce suit ses propres règles de déplacement, notamment le Général, le Garde, 
l’Éléphant, le Cheval, le Canon, la Tour et le Soldat. Les joueurs peuvent sélectionner une pièce puis cliquer 
sur une case valide pour effectuer un déplacement. Les déplacements illégaux sont automatiquement bloqués. 
Le jeu alterne les tours et se termine lorsque l'un des Généraux est capturé ( situation d'échec et mat ).

Lancement du jeu : ---------------------------------------------------------------------------------------------

Pour lancer le jeu, il faut copier le contenu du dossier "Chess" dans Eclipse ( créer un nouveau projet et 
intégrer le contenu du dossier dans "src" ). Enfin, il faudra simplement aller dans la classe "MenuPrincipal" 
( dans le package "gui" ) et lancer le programme avec la touche "run" ( ctrl + f11 sur Windows )

Utilisation : --------------------------------------------------------------------------------------------------

Après le lancement de l’application, un affichage invite le joueur à choisir son mode de jeu. Une fois le mode de 
jeu choisi, un plateau s'affiche avec l'ensemble des pièces en place. Un joueur clique 
sur une pièce pour la sélectionner, puis clique sur la case de destination. 
Si le déplacement est conforme aux règles du Xiangqi, la pièce se déplace, sinon l’action est ignorée. 
Le jeu continue jusqu’à la capture du Général adverse, ce qui met fin à la partie.

Technologies utilisées : ---------------------------------------------------------------------------------------

Le projet a été réalisé en Java et utilise la bibliothèque Swing pour la création de l’interface graphique et la 
gestion des événements utilisateurs.

