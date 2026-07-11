# Istruzioni di utilizzo

## Generazione del parser

Per generare il parser è necessario eseguire il seguente comando Gradle:

```bash
gradle generateParser
```

## Esecuzione dell'interprete

Per eseguire l'interprete su un file sorgente `.codine`, utilizzare il comando:

```bash
gradle interpreter -Pfile=path/file.codine
```

dove `path/file.codine` rappresenta il percorso del file che si desidera interpretare.

## Esecuzione dei test

Per eseguire automaticamente una serie di test in sequenza è disponibile lo script:

```bash
run-tests.sh
```

Lo script esegue i programmi presenti nella directory:

```
tests/
```

Questa directory deve contenere esclusivamente programmi **corretti**.

Per testare invece programmi contenenti errori (di sintassi o di altro tipo) è necessario utilizzare:

```bash
run-err-tests.sh
```

Questo script funziona in modo analogo al precedente, ma opera sulla directory:

```
err-tests/
```

La directory `err-tests/` è destinata alla raccolta di programmi utilizzati per verificare la corretta gestione degli errori da parte dell'interprete.

# Note sullo sviluppo

## Utilizzo dell'intelligenza artificiale

Durante lo sviluppo del progetto è stata utilizzata l'intelligenza artificiale come supporto in alcune fasi del lavoro.

In particolare, è stata impiegata per la generazione di alcuni programmi di test contenenti input utili alla verifica del comportamento dell'interprete e nella creazione dei file di scripting per l'esecuzione in sequenza dei programmi. 

L'intelligenza artificiale è stata inoltre utilizzata in misura limitata come strumento di supporto durante lo sviluppo software.