# Spring Boot JWT Starter Kit

Starter kit Spring Boot avec authentification JWT.

## 🚀 Récupération du projet

```bash
git clone <votre-repo>
cd jwt
```

## ⚙️ Configuration

### 1. Générer la clé secrète JWT

```bash
openssl rand -base64 64
```

### 2. Configurer les variables d'environnement

Copiez le fichier `env.example` vers `.env` :

```bash
cp env.example .env
```

Modifiez le fichier `.env` avec vos valeurs :
- `DB_URL` : URL de connexion MySQL
- `DB_USERNAME` : Utilisateur MySQL
- `DB_PASSWORD` : Mot de passe MySQL
- `JWT_SECRET` : Clé secrète générée à l'étape 1
- `JWT_EXPIRATION_SECOND` : Durée de validité du token (en secondes, ex: 86400 pour 24 heures)

## 🔧 Personnaliser le nom du projet

### 1. Modifier `pom.xml`

```xml
<groupId>com.votre-entreprise</groupId>
<artifactId>votre-projet</artifactId>
<name>votre-projet</name>
<description>votre-projet</description>
```

### 2. Renommer les packages

1. Renommez le dossier `src/main/java/com/edouard/jwt/` vers `src/main/java/com/votre-entreprise/votre-projet/`
2. Mettez à jour tous les `package` dans les fichiers Java
3. Modifiez `spring.application.name` dans `application.properties`

### 3. Renommer la classe principale

Renommez `JwtApplication.java` si nécessaire et mettez à jour la référence dans `pom.xml`.

## ▶️ Lancer l'application

```bash
./mvnw spring-boot:run
```

L'application sera accessible sur `http://localhost:8080`

## 📦 Prérequis

- Java 17+
- Maven
- MySQL

