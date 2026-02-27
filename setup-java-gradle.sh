#!/bin/bash
# Script per configurare un progetto Java 21 con Gradle, JUnit, Mockito e JavaFX
# Versione: usa la cartella corrente

if [ -z "$1" ]; then
  echo "Uso: $0 <package-base> (es. ./setup-java-gradle.sh com.esempio.gestioneutenti)"
  exit 1
fi

PACKAGE=$1
PACKAGE_PATH=$(echo "$PACKAGE" | tr '.' '/')
PROJECT_NAME=$(basename "$PWD")

echo "📁 Configurazione progetto nella cartella corrente: $PROJECT_NAME"
echo "📦 Package: $PACKAGE"

# Crea struttura cartelle
mkdir -p src/{main,test}/{java,resources}
mkdir -p src/main/java/$PACKAGE_PATH
mkdir -p src/test/java/$PACKAGE_PATH

# build.gradle
cat > build.gradle <<EOF
plugins {
    id 'java'
    id 'application'
}

group = '${PACKAGE%.*}'
version = '1.0.0'

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

application {
    mainClass = '${PACKAGE}.App'
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.apache.commons:commons-lang3:3.14.0'

    // JavaFX
    def javaFxVersion = '21.0.4'
    implementation "org.openjfx:javafx-controls:\${javaFxVersion}"
    implementation "org.openjfx:javafx-fxml:\${javaFxVersion}"
    implementation "org.openjfx:javafx-graphics:\${javaFxVersion}"

    // Test
    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.11.0'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.11.0'
    testImplementation 'org.mockito:mockito-core:5.13.0'
}

test {
    useJUnitPlatform()
}
EOF

# settings.gradle
echo "rootProject.name = '${PROJECT_NAME}'" > settings.gradle

# App.java
cat > src/main/java/$PACKAGE_PATH/App.java <<EOF
package ${PACKAGE};

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        stage.setScene(new Scene(new Label("Ciao da JavaFX + Gradle!"), 300, 100));
        stage.setTitle("App JavaFX");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
EOF

# AppTest.java
cat > src/test/java/$PACKAGE_PATH/AppTest.java <<EOF
package ${PACKAGE};

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AppTest {
    interface Servizio { String saluta(); }

    @Test
    void testConMockito() {
        Servizio mockServizio = mock(Servizio.class);
        when(mockServizio.saluta()).thenReturn("Ciao test!");
        assertEquals("Ciao test!", mockServizio.saluta());
    }
}
EOF

# .gitignore
cat > .gitignore <<EOF
/ build/
/.gradle/
/out/
*.class
*.log
EOF

# Git init (solo se non esiste già)
if [ ! -d ".git" ]; then
    git init -q
    git add .
    git commit -m "Inizializzazione progetto Java 21 Gradle con JavaFX e Mockito" -q
fi

# Gradle wrapper
if command -v gradle >/dev/null 2>&1; then
    gradle wrapper --gradle-version 8.10
    echo "✅ Gradle wrapper generato (versione 8.10)."
else
    echo "⚠️ Gradle non trovato. Installa Gradle per generare il wrapper."
fi

echo "✅ Progetto configurato nella cartella corrente."
echo "➡️  Per eseguire:"
echo "   ./gradlew run"