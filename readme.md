CyclesMod
=========

CyclesMod &egrave; un'utility per il vecchio videogioco per DOS **The Cycles: International Grand Prix Racing** (DSI/Accolade, 1989), che consente di modificare e personalizzare le configurazioni delle moto, di norma non accessibili dall'utente.

*The Cycles* permette al giocatore di scegliere una tra tre moto disponibili: 125, 250 o 500 cc, dunque una per categoria. I parametri di funzionamento delle moto sono contenuti nel file binario **`BIKES.INF`** fornito col gioco stesso. CyclesMod interpreta questo file e mette a disposizione del giocatore un'interfaccia grafica con quale potr&agrave; esaminare e modificare la configurazione delle tre moto. Al salvataggio del file, il programma rigenera un `BIKES.INF` utilizzabile da *The Cycles*, contenente la configurazione modificata.


### Installazione e configurazione di base

**Si raccomanda di effettuare una copia di backup del gioco prima di utilizzare il presente programma, perch&egrave; pu&ograve; sempre capitare di sovrascrivere inavvertitamente uno dei file originali del gioco.**

1. [scaricare](http://github.com/Albertus82/CyclesMod/releases) una release `bin` in formato ZIP adatta al proprio sistema operativo. La scelta &egrave; tra Windows x86 (32 e 64 bit) e Linux x86 (32 e 64 bit);
2. scompattare il file ZIP in una cartella a piacimento;

Per avviare l'applicazione &egrave; richiesta la presenza della variabile di ambiente `JAVA_HOME` e di [Java Runtime Environment](http://www.java.com) (JRE) versione 6 (1.6) o successiva.

In ambiente **Windows** &egrave; sufficiente richiamare il file batch **`cyclesmod-win??-win.bat`** (dove `??` equivale a `32` o a `64` a seconda della versione scaricata) senza specificare altro; analogamente su **Linux** basta richiamare lo script shell **`cyclesmod-lnx??-win.sh`**, avendogli prima assegnato il permesso di esecuzione con un comando del tipo `chmod 754 cyclesmod-lnx??-win.sh`.

All'apertura del programma sar&agrave; mostrata una finestra contenente i valori predefiniti delle moto. Sono disponibili tre schede, una per categoria (125, 250 e 500 cc). Ogni scheda &egrave; suddivisa in tre sezioni:
* Impostazioni
* Cambio
* Coppia
&Egrave; inoltre presente un grafico della curva di coppia generato in tempo reale in base ai valori di coppia presenti nella relativa sezione.


#### Impostazioni
Ogni moto dispone di otto impostazioni generali che determinano le seguenti caratteristiche:
* **Numero marce**: il numero di rapporti del cambio di velocit&agrave; (sono ammessi valori compresi tra `0` e `9`).
* **Regime inizio zona rossa**: 
* **Regime massimo**: 
* **Tolleranza fuorigiri**: 
* **Aderenza**: 
* **Velocit&agrave; di frenata**: 
* **Soglia testacoda**: 
* **Regime di scalata**: significativo solo nei livelli di difficolt&agrave; pi&ugrave; bassi che prevedono il cambio automatico, determina il regime al di sotto del quale il cambio automatico innesta la marcia inferiore, se disponibile.
Sono inoltre presenti tre valori di significato sconosciuto, probabilmente irrilevanti, che &egrave; comunque possibile modificare a scopo sperimentale.

#### Cambio
&Egrave; possibile configurare i rapporti del cambio di velocit&agrave; per ogni singola marcia. A valori pi&ugrave; elevati corrispondono rapporti pi&ugrave; corti. I valori ammessi sono compresi tra `0` e `65535`.

#### Coppia
La curva di coppia utilizzata dal gioco viene costruita a partire dai valori presenti in questa sezione. Ogni valore rappresenta la coppia del motore in Nm a un determinato regime indicato nell'etichetta posta sulla sinistra della casella contenente il valore. I valori ammessi sono compresi tra `0` e `255`.
