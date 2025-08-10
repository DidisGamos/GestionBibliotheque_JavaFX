
# 💧⚡ Gestion de paiement d’eau et d’électricité

## 📌 Description
Application PHP permettant de gérer la facturation et le paiement des consommations d’eau et d’électricité des clients.  
Elle inclut la gestion des clients, compteurs, relevés, calcul automatique des montants dus, et suivi des paiements.

## 🚀 Fonctionnalités
- Gestion des clients (ajout, modification, suppression)  
- Gestion des compteurs (eau et électricité)  
- Enregistrement des relevés de consommation  
- Calcul automatique des factures (consommation × prix unitaire)  
- Gestion des paiements et historique  
- Génération de reçus PDF (optionnel)  
- Statistiques et rapports  

## 🛠️ Technologies utilisées
- PHP  
- MySQL  
- HTML, CSS, Bootstrap, JavaScript  
- XAMPP (serveur local)  

## 📂 Structure du projet

## ⚡ Installation & Utilisation

1. **Cloner le dépôt**  
```bash
git clone https://github.com/DidisGamos/PHP_Project.git

```

2. **Copier le projet dans le dossier XAMPP**  
   Exemple : C:/xampp/htdocs/project_php


3. **Importer la base de données**  
- Ouvrir **phpMyAdmin** via : [http://localhost/phpmyadmin](http://localhost/phpmyadmin)  
- Créer une base de données (exemple : `gestion_paiement`)  
- Importer le fichier SQL situé dans `/php_brute/db/payment.sql`

4. **Configurer la connexion à la base dans le projet**  
Modifier le fichier :  /php_brute/src/config.php

avec tes paramètres (hôte, utilisateur, mot de passe, nom de la base)

5. **Démarrer le serveur XAMPP**  
Lancer **Apache** et **MySQL** via le panneau de contrôle XAMPP

6. **Accéder à l’application**  
Ouvrir dans un navigateur :  http://localhost/project_php


---

## 📸 Aperçu  
<img width="2160" height="1479" alt="work1" src="https://github.com/user-attachments/assets/c8dde055-34f9-4eed-94e1-f16e59c66843" />


---

## 📄 Auteur  
Didis Gamos — [@DidisGamos](https://github.com/DidisGamos)

