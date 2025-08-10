# 📚 Gestion d’une bibliothèque - JavaFX

## 📌 Description  
Application desktop en JavaFX pour gérer une bibliothèque : gestion des livres, membres, prêts et retours.

## 🚀 Fonctionnalités  
- Gestion des livres (ajout, modification, suppression)  
- Gestion des membres  
- Gestion des prêts et retours  
- Recherche et filtres  
- Statistiques diverses  

## 🛠️ Technologies utilisées  
- Java  
- JavaFX  
- PostgreSQL  
- JDBC (connexion base de données)  
- IDE recommandé : IntelliJ IDEA  

## 📂 Structure du projet  

/src # Code source Java
/db # Scripts et fichiers de base de données
README.md # Documentation

---


## 🗄️ Base de données PostgreSQL  
Le fichier de base de données est situé ici : ..\GestionBibliothequeAllFrancaise\db\DB_Biblio

---


### Installation de la base PostgreSQL  
1. Installer PostgreSQL (si ce n’est pas déjà fait).  
2. Ouvrir pgAdmin ou un terminal.  
3. Créer une nouvelle base de données (exemple : `bibliotheque`).  
4. Importer le script SQL situé dans `DB_Biblio` :  
   - via pgAdmin : clic droit sur la base → Restore ou Import → choisir le fichier SQL.  
   - ou en ligne de commande :  
   ```bash
   psql -U utilisateur -d bibliotheque -f "..\GestionBibliothequeAllFrancaise\db\DB_Biblio\script.sql"

⚡ Installation & utilisation
1. Cloner le dépôt :

```bash
git clone https://github.com/DidisGamos/GestionBibliotheque_JavaFX.git

```

---

2. Importer la base PostgreSQL comme indiqué ci-dessus.

3. Configurer la connexion JDBC dans le projet Java (exemple dans config.properties ou directement dans le code) :
```bash
db.url=jdbc:postgresql://localhost:5432/bibliotheque
db.user=ton_utilisateur
db.password=ton_mot_de_passe

```

4. Ouvrir le projet dans IntelliJ IDEA ou un autre IDE Java.

5. Compiler et lancer l’application.

📸 Aperçu
<img width="2160" height="1436" alt="work2" src="https://github.com/user-attachments/assets/4bfc9525-6c47-4a82-80cd-ce4aac6df232" />


📄 Auteur
Didis Gamos — @DidisGamos

---

## 📄 Auteur  
Didis Gamos — [@DidisGamos](https://github.com/DidisGamos)