# Communication Morse entre deux STM32F4

Projet d'introduction aux microcontrôleurs (groupe A3, Cergy Paris Université, 2025-2026).
Dania Hamitouche, Imane Moussaoui, Odessa Triollet-Pereira, Alexis Sade, Bilel Ben Naya.

- `MC_EMET_SON` : carte émettrice. Reçoit un texte par UART, le convertit en Morse et pilote un buzzer.
- `MC_RECEPT_SON` : carte réceptrice. Lit un microphone avec l'ADC, mesure la durée des sons et reconstruit le texte, renvoyé par UART.
- `Rapport_Microcontroleur.pdf` et `Presentation.pdf` : rapport et diaporama.

Démo dans le navigateur : https://bilel-ben-naya.github.io/demos/morse/index.html
