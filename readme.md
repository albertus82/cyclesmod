CyclesMod
=========

**The Cycles - International Grand Prix Racing** &egrave; un videogioco di simulazione motociclistica. Risalente al 1989, fu distribuito per tutte le pi&ugrave; diffuse piattaforme dell'epoca, dal Commodore 64 al PC IBM (DOS), ed &egrave; oggi facilmente reperibile come *Abandonware*.

CyclesMod &egrave; un *mod* per la versione DOS di questo gioco, che consente di **modificare le configurazioni delle moto**, di norma non accessibili all'utente.

Il gioco permette di scegliere tre diverse moto: 125, 250 o 500 cc. I parametri di funzionamento delle moto sono contenuti nel file binario **`BIKES.INF`** che &egrave; uno dei file del gioco stesso. **CyclesMod** carica e interpreta questo file, e mette a disposizione del giocatore un'interfaccia grafica con quale pu&ograve; esaminare e modificare la configurazione delle tre moto. Al salvataggio del file, il programma genera un nuovo `BIKES.INF` utilizzabile da *The Cycles*, contenente la configurazione modificata.


### Installazione

**Si raccomanda di effettuare una copia di backup del gioco prima di utilizzare questo programma!**

1. [scaricare](http://github.com/Albertus82/CyclesMod/releases) una release `bin` in formato ZIP adatta al proprio sistema operativo. La scelta &egrave; tra Windows x86 (32 e 64 bit) e Linux x86 (32 e 64 bit);
2. scompattare il file ZIP in una cartella a piacimento.

Per avviare l'applicazione &egrave; richiesta la presenza della variabile di ambiente `JAVA_HOME` e di [Java Runtime Environment](http://www.java.com) (JRE) versione 6 (1.6) o successiva.

In ambiente **Windows** &egrave; sufficiente richiamare il file batch **`cyclesmod-gui.bat`** senza specificare altro; analogamente su **Linux** basta richiamare lo script shell **`cyclesmod-gui.sh`**, avendogli prima assegnato il permesso di esecuzione con un comando del tipo `chmod 754 cyclesmod-gui.sh`.

All'apertura del programma comparir&agrave; una finestra contenente i valori predefiniti delle moto. 


### Configurazione delle moto

La finestra principale dell'applicazione presenta tre schede, una per categoria di moto: 125, 250 e 500 cc. Ogni scheda &egrave; suddivisa in tre sezioni:
* **Impostazioni** generali
* **Cambio** di velocit&agrave;
* **Coppia** motrice

&Egrave; inoltre presente un grafico della curva di coppia, generato in tempo reale in base ai valori di coppia motrice presenti nella relativa sezione.

#### Impostazioni
Ogni moto dispone di otto impostazioni generali che determinano le seguenti caratteristiche:
* **Numero marce**: il numero di rapporti del cambio di velocit&agrave; (sono ammessi valori compresi tra `0` e `9`).
* **Regime inizio zona rossa**: regime oltre il quale, dopo un tempo variabile configurabile, si verifica la rottura del motore.
* **Regime massimo**: regime massimo raggiungibile dal motore (limitatore).
* **Tolleranza fuorigiri**: periodo di grazia durante il quale il motore non si guasta nonostante giri ad un regime maggiore del *Regime inizio zona rossa*; il valore &egrave; espresso in unit&agrave; di misura temporale variabile a seconda del livello di difficolt&agrave;.
* **Aderenza**: soglia di slittamento della moto, che determina la velocit&agrave; con cui &egrave; possibile affrontare le curve.
* **Velocit&agrave; di frenata**: determina il tempo di arresto della moto.
* **Soglia testacoda**: facilit&agrave; con cui la moto va in testacoda mentre si percorre una curva.
* **Regime di scalata**: significativo solo nei livelli di difficolt&agrave; pi&ugrave; bassi che prevedono il cambio automatico, determina il regime al di sotto del quale il cambio automatico innesta la marcia inferiore, se disponibile.
Sono inoltre presenti tre valori di significato sconosciuto, probabilmente irrilevanti, che &egrave; comunque possibile modificare a scopo sperimentale.

#### Cambio
&Egrave; possibile configurare i rapporti del cambio di velocit&agrave; per ogni singola marcia. A valori pi&ugrave; elevati corrispondono rapporti pi&ugrave; corti. I valori ammessi sono compresi tra `0` e `65535`.

#### Coppia
La curva di coppia utilizzata dal gioco viene costruita a partire dai valori presenti in questa sezione. Ogni valore rappresenta la coppia del motore in Nm a un determinato regime indicato nell'etichetta posta sulla sinistra della casella contenente il valore. I valori ammessi sono compresi tra `0` e `255`.
