CyclesMod
=========

**The Cycles - International Grand Prix Racing** &egrave; un videogioco di simulazione motociclistica. Risalente al 1989, fu distribuito per tutte le pi&ugrave; diffuse piattaforme dell'epoca, dal Commodore 64 al PC IBM (DOS), ed &egrave; oggi facilmente reperibile come *Abandonware*.

**CyclesMod** &egrave; un *mod* per la versione DOS di questo gioco, che consente di **modificare le configurazioni delle moto**, di norma non accessibili all'utente.

![cyclesmod_221](https://cloud.githubusercontent.com/assets/8672431/13903500/3bac8840-ee7e-11e5-97fe-eeda1c7bb896.png)

Il gioco permette di scegliere tre diverse moto: 125, 250 o 500 cc. I parametri di funzionamento delle moto sono contenuti nel file binario **`BIKES.INF`** che &egrave; uno dei file del gioco stesso. **CyclesMod** carica e interpreta questo file, e mette a disposizione del giocatore un'interfaccia grafica con quale pu&ograve; esaminare e modificare la configurazione delle tre moto. Al salvataggio del file, il programma genera un nuovo `BIKES.INF` utilizzabile da *The Cycles*, contenente la configurazione modificata.

Trattandosi di un gioco molto vecchio, naturalmente non pu&ograve; essere eseguito nativamente con i sistemi operativi correnti, ma con l'aiuto di macchine virtuali o, meglio ancora, di specifici emulatori come [**DOSBox**](http://www.dosbox.com), &egrave; possibile comunque eseguirlo con ottimi risultati.

>L'icona dell'applicazione &egrave; stata realizzata da [Everaldo Coelho](http://www.everaldo.com) (licenza [LGPL](http://www.gnu.org/licenses/lgpl.html)), e prelevata da [Iconfinder](http://www.iconfinder.com).


### Installazione

1. [scaricare](http://github.com/Albertus82/CyclesMod/releases) una release `bin` in formato ZIP adatta al proprio sistema operativo. La scelta &egrave; tra Windows, Linux e OS X (sia 32 che 64 bit);
2. scompattare il file ZIP in una cartella a piacimento.

Per avviare l'applicazione &egrave; richiesto [Java Runtime Environment](http://www.java.com) (JRE) versione 6 (1.6) o successiva. Se la variabile di ambiente `JAVA_HOME` viene rilevata, essa sar&agrave; utilizzata come riferimento per avviare la Java Virtual Machine, in caso contrario sar&agrave; richiamato direttamente l'eseguibile `java` (o `javaw`).

In ambiente **Windows** &egrave; sufficiente richiamare il file batch [**`cyclesmod.bat`**](src/main/scripts/cyclesmod.bat) senza specificare altro; analogamente su **Linux** e **OS X** basta richiamare rispettivamente lo script shell [**`cyclesmod.sh`**](src/main/scripts/cyclesmod.sh) o [**`cyclesmod.command`**](src/main/scripts/cyclesmod.command), avendogli prima assegnato il permesso di esecuzione con un comando del tipo `chmod 754 cyclesmod.sh` o `chmod 754 cyclesmod.command`.


### Utilizzo

**Si raccomanda di effettuare una copia di backup del gioco prima di utilizzare questo programma!**

La finestra principale dell'applicazione presenta tre schede, una per categoria di moto: 125, 250 e 500 cc. Ogni scheda &egrave; suddivisa in tre sezioni:
* **Impostazioni** generali
* **Cambio** di velocit&agrave;
* **Coppia** motrice

&Egrave; inoltre presente un grafico della curva di coppia, generato in tempo reale in base ai valori di coppia motrice presenti nella relativa sezione.

Ogni sezione presenta diverse propriet&agrave;, tutte rigorosamente numeriche e corredate da etichette descrittive sulla sinistra. Quando si modifica il valore di una propriet&agrave;, esso viene mostrato in grassetto per evidenziare che &egrave; diverso dal valore predefinito. Volendo conoscere il valore predefinito di una singola propriet&agrave;, &egrave; sufficiente portare il mouse sul relativo campo e attendere la comparsa del suggerimento.

All'apertura del programma, tutte le maschere vengono precaricate con i valori predefiniti per tutte moto.

Nella parte bassa della finestra sono presenti quattro pulsanti:
* **Carica**: carica un file `BIKES.INF` esistente.
* **Salva con nome...**: salva le impostazioni correnti in un file `BIKES.INF`.
* **Reimposta**: ripristina le impostazioni predefinite per tutte le moto; qualsiasi personalizzazione non salvata viene cancellata.
* **?**: Visualizza informazioni sulla versione di *CyclesMod*.

Dopo aver modificato le propriet&agrave; desiderate, &egrave; sufficiente fare clic sul pulsante *Salva con nome...* e salvare il file `BIKES.INF` nella directory di *The Cycles*. Quasi sicuramente il file sar&agrave; gi&agrave; presente, pertanto verr&agrave; richiesto di sovrascriverlo, quindi **&egrave; bene accertarsi di avere una copia di riserva del file che si sovrascriver&agrave;**. A questo punto &egrave; possibile eseguire il gioco in un emulatore DOS come DOSBox. La configurazione viene ricaricata ogni volta che si inizia una nuova gara, pertanto non occorre riavviare completamente il gioco.

###### Ripristino del `BIKES.INF` originale

Nel caso in cui si volessero ripristinare le configurazioni originali di tutte le moto, &egrave; sufficiente fare clic sul pulsante *Reimposta*, che provveder&agrave; a ricaricare le impostazioni del `BIKES.INF` originale fornito insieme al gioco. Baster&agrave; quindi fare clic su *Salva con nome...* per poter sovrascrivere qualsiasi `BIKES.INF` personalizzato con la versione originale.


### Configurazione delle moto

#### Impostazioni generali
Ogni moto dispone di otto impostazioni generali che determinano le seguenti caratteristiche:
* **Numero marce**: il numero di rapporti del cambio di velocit&agrave;. Intervallo di valori validi: da `0` a `9`.
* **Regime inizio zona rossa**: regime oltre il quale, dopo un tempo variabile determinato dall'impostazione *Tolleranza fuorigiri* e dal livello di difficolt&agrave; impostato, si verifica la rottura del motore. Ai livelli di difficolt&agrave; 1 e 2 (i pi&ugrave; bassi), questo valore determina anche il regime di cambiata. Intervallo di valori validi: `8500`-`32767` giri/min.
* **Regime massimo**: regime massimo raggiungibile dal motore (limitatore). Intervallo di valori validi: `768`-`14335` giri/min. Notare che, una volta raggiunto il limite, la moto continua ad accelerare con una coppia motrice pari a quella impostata per il regime limite, pertanto, per limitare efficacemente il regime ad un certo valore, occorre azzerare i valori della [coppia](#coppia-motrice) nell'intorno del regime limite.
* **Tolleranza fuorigiri**: periodo di grazia durante il quale il motore non si guasta nonostante giri ad un regime maggiore del *Regime inizio zona rossa*; il valore &egrave; espresso in un'unit&agrave; di misura del tempo *lineare* variabile a seconda del livello di difficolt&agrave;. Intervallo di valori validi: `0`-`32767`.
Segue un elenco per la determinazione del valore in base ai secondi di tolleranza desiderati a seconda del livello di difficolt&agrave;:
  * Livello 1/5 (Beg.): il motore non si guasta mai, a meno di non impostare `0`.
  * Livello 2/5: 1 sec. = `50` (&egrave; comunque attivo il cambio automatico).
  * Livello 3/5: 1 sec. = `80`.
  * Livello 4/5: 1 sec. = `120`.
  * Livello 5/5 (Pro): 1 sec. = `160`.
* **Aderenza**: soglia di slittamento della moto, che determina la velocit&agrave; con cui &egrave; possibile affrontare le curve. Valori validi compresi tra `0` (la moto slitta immediatamente e non curva affatto) e `65535` (la moto non slitta mai).
* **Velocit&agrave; di frenata**: determina il tempo di arresto della moto. Valori validi compresi tra `0` (la moto non frena, anzi si azzerano addirittura gli attriti e la resistenza aerodinamica) e `65535` (la moto si arresta istantaneamente alla minima frenata).
* **Soglia testacoda**: facilit&agrave; con cui la moto va in testacoda mentre si percorre una curva slittando. Valori validi compresi tra `0` (la moto va in testacoda al primo accenno di slittamento) e `65535` (la moto non va mai in testacoda).
* **Regime di scalata**: significativo solo nei livelli di difficolt&agrave; pi&ugrave; bassi che prevedono il cambio automatico, determina il regime al di sotto del quale il cambio automatico innesta la marcia inferiore, se disponibile. Valori validi compresi tra `0` (non scala mai) e `32767` giri/min. (scala in continuazione, di fatto rende impossibile la cambiata).

>Sono inoltre presenti tre valori di significato sconosciuto, probabilmente irrilevanti, che &egrave; comunque possibile modificare a scopo sperimentale.

#### Cambio di velocit&agrave;
&Egrave; possibile configurare i rapporti del cambio di velocit&agrave; per ogni singola marcia. A valori pi&ugrave; elevati corrispondono rapporti pi&ugrave; corti. I valori ammessi sono compresi tra `0` e `65535` per le marce da 1 a 9, mentre per marcia *N* (folle) il valore &egrave; irrilevante.

#### Coppia motrice
La curva di coppia del motore viene costruita a partire dai valori presenti in questa sezione. Ogni valore rappresenta la coppia del motore in N&middot;m a un determinato regime indicato nell'etichetta posta sulla sinistra della casella contenente il valore. I valori ammessi sono compresi tra `0` e `255` N&middot;m. La curva risultante viene rappresentata graficamente nel relativo riquadro.

### Versione da riga di comando

Se, per qualsiasi motivo, si preferisce operare senza interfaccia grafica, &egrave; disponibile una versione dell'applicazione che funziona da riga di comando, certamente molto meno comoda rispetto alla versione con interfaccia grafica, ma comunque pienamente funzionante.
Gli script di avvio ([**`cyclesmod.bat`**](src/main/scripts/cyclesmod.bat), [**`cyclesmod.sh`**](src/main/scripts/cyclesmod.sh) e [**`cyclesmod.command`**](src/main/scripts/cyclesmod.command)) prevedono un'apposita opzione per avviare l'applicazione in questa modalit&agrave; "console":
* Windows: **`cyclesmod.bat -c`**
* Linux: **`cyclesmod.sh -c`**
* OS X: **`cyclesmod.command -c`**

Una volta eseguito, il programma verifica per prima cosa l'esistenza di un file di testo denominato `BIKES.CFG`; se non presente, ne crea uno di default a partire dal file binario `BIKES.INF` originale. Il file `BIKES.CFG` &egrave; in pratica una "traduzione" in testo semplice del file `INF`; aprendolo con un editor di testo, &egrave; possibile accedere direttamente ai parametri delle moto, i quali sono abbastanza autoesplicativi e suddivisi nei soliti tre gruppi: *impostazioni generali* (settings), *cambio di velocit&agrave;* (gearbox) e *coppia motrice* (torque). Dunque, inizialmente il file `CFG` conterr&agrave; i valori predefiniti del gioco, derivando direttamente dal file `INF` originale.

A seguire, il programma rileva l'esistenza del file `BIKES.CFG`, ne legge il contenuto e infine produce un nuovo file `BIKES.INF`, sovrascrivendo quello eventualmente preesistente. A questo punto &egrave; sufficiente copiare nella directory del gioco il file `BIKES.INF` generato, sovrascrivendo il file preesistente, **avendone fatto prima una copia di backup**. Avviando il gioco sar&agrave; quindi possibile sperimentare le modifiche apportate alla configurazione.
>In realt&agrave; una copia compressa del file [`BIKES.INF`](/src/main/resources/it/albertus/cycles/data/bikes.zip) originale &egrave; contenuta anche nel JAR di *CyclesMod*, quindi sarebbe comunque possibile recuperarlo sia estraendolo direttamente da l&igrave;, che cancellando il file `CFG` e facendo girare nuovamente il programma "a vuoto", il quale quindi generer&agrave; automaticamente un `CFG` predefinito e un `INF` analogo a quello originale.

Per effettuare modifiche alle moto, &egrave; quindi sufficiente aprire il file `CFG`, modificare i parametri di interesse, salvare il file e far girare *CyclesMod*. Il programma rilever&agrave; l'esistenza del file BIKES.CFG e produrr&agrave; un nuovo BIKES.INF contenente le modifiche apportate. In caso di errori saranno mostrati opportuni messaggi in console.
