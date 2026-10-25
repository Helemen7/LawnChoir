# Modulo Magisk per Lawnchair (animazioni di apertura/chiusura app interrompibili)

Il file da installare è `lawnchair_quickstep_magisk.zip`, nella stessa cartella di questa guida. Dentro ci sono tre cose: Lawnchair installato come app di sistema privilegiata, l'elenco dei permessi privilegiati che gli servono e un overlay che dice ad Android di usare Lawnchair come app delle recenti.

## Cosa deve essere vero prima di iniziare

- PixelOS è installato e parte, e Magisk funziona (hai già Zygisk e il fix per Play Integrity).
- Hai aperto le Opzioni sviluppatore e attivato il Debug USB, e hai autorizzato questo PC quando il telefono lo chiede.
- Hai fatto una copia dei dati importanti. Se qualcosa va storto non dovresti perdere i dati, ma meglio non scoprirlo.

Per controllare il collegamento, da PowerShell nella cartella del progetto:

    .\platform-tools-dl\platform-tools\adb.exe devices

Deve comparire il telefono come `device`, non `unauthorized`.

## Installazione

1. Copia `lawnchair_quickstep_magisk.zip` sul telefono.
2. In Magisk apri Moduli, scegli "Installa da memoria" e seleziona lo zip.
3. Riavvia. Il primo avvio può durare più del solito perché Android ricompila l'app.
4. Alla pressione del tasto Home scegli Lawnchair e imposta "Sempre". Se non te lo chiede, vai in Impostazioni, App, App predefinite, App Home.

## Come controllare che funzioni

Con il telefono collegato:

    adb shell cmd overlay list android

Deve comparire `app.lawnchair.overlay.recents` con la spunta `[x]`.

    adb shell dumpsys package app.lawnchair.nightly.debug

Cerca `privileged` tra i flag e `CONTROL_REMOTE_APP_TRANSITION_ANIMATIONS: granted=true`.

Se la seconda riga non c'è, Lawnchair non è riconosciuto come app delle recenti e le mie modifiche non girano. In quel caso mandami l'output di:

    adb logcat -d -s LawnchairApp

Per vedere cosa decide il codice delle animazioni, apri un'app dall'icona e subito prima che finisca di aprirsi premi Indietro oppure Home. Poi:

    adb logcat -d -s QuickstepTransitionManager RemoteAnimRunnerCompat LauncherAnimationRunner

Le righe dicono se l'interruzione è stata accettata (`Merge accepted`) oppure perché è stata rifiutata. Mandami quell'output.

## Se il telefono non parte più

La via sicura, che funziona sempre, è togliere Magisk riflashando il `boot.img` originale della stessa build (non quello patchato):

    fastboot flash boot boot.img

Con il boot originale Magisk non carica i moduli, quindi il telefono torna a partire. Poi puoi rimettere il boot patchato e togliere il modulo dall'app di Magisk, o rifare l'installazione dopo che mi hai mandato l'errore.

Altre due vie, più comode ma non le ho verificate su questo telefono: avviare in modalità provvisoria (di solito tieni premuto Volume giù durante l'animazione di avvio, e Magisk disattiva i moduli), oppure con il telefono collegato `adb wait-for-device shell magisk --remove-modules`.

## Stato attuale sul telefono (25 ottobre 2026)

Il modulo installato è quello dentro `lawnchair_quickstep_magisk.zip` (hash `e92ade50…c1a5`). Dopo un riavvio completo il telefono parte da solo su Lawnchair, la navigazione a gesti è attiva e SystemUI è collegato al servizio dei gesti di Lawnchair.

Per arrivare qui ho dovuto cambiare Lawnchair, perché il ramo `16-dev` è compilato contro le API interne di Android 16 e il tuo PixelOS è Android 17. Le correzioni sono nel codice in WSL (`/home/aosp/lawnchair`):

- `InputConsumerController`: in Android 17 `createInputConsumer` restituisce il canale invece di riempirlo.
- Risorse private del framework (`taskbar_size`, `taskbar_phone_size`, `config_wallpaperMaxScale`, `taskbar_phone_rounded_corner_content_margin`): sostituite con i valori di Android 17, perché il riferimento per ID non è stabile tra versioni.
- Flag desktop (`DesktopModeFlags`, `DesktopExperienceFlags`) e flag aconfig spostati sotto `com.android.internal.hidden_from_bootclasspath`: sostituiti da classi locali che chiedono il valore al framework e, se il flag non esiste più, lo considerano attivo.
- Miniature delle recenti: usano `android.window.TaskSnapshotManager`.

Cose che ho cambiato sul telefono, tutte reversibili:

- Il Pixel Launcher è disattivato per il tuo utente (`pm disable-user`). Crashava in loop per un permesso mancante (`GET_ROLE_HOLDERS`) e faceva comparire il dialogo "keeps stopping". Per riattivarlo: `adb shell pm enable com.google.android.apps.nexuslauncher`.
- Lawnchair è l'app Home predefinita.
- Lawnchair è stato aggiornato anche come app installata sopra il modulo (stessa firma, resta app di sistema privilegiata).

Backup e strumenti in questa cartella: `backup-phone\` (boot e vendor_boot attuali, screenshot, elenchi dei mismatch tra API 16 e 17) e `tools\cycle.ps1` (compila, installa e controlla se il servizio dei gesti è su).

Correzione dei blocchi alla Home e alle recenti: in Android 17 l'interfaccia `IRecentsAnimationRunner.onAnimationStart` non ha più il parametro `minimizedHomeBounds`, che Lawnchair leggeva. La transazione falliva, il Launcher non partiva mai con l'animazione e dopo 5 secondi arrivava `Recents animation start has been pending`. Ora `SystemUiProxy.kt` legge a mano il formato di Android 17. Allo stesso modo `onBackEvent` verso SystemUI ha un `displayId` in più. Il resto delle differenze tra le interfacce AIDL (bolle, PiP, desktop, split) è elencato in `backup-phone\aidl-diff-details.txt` e riguarda funzioni che non usi.

Interruzione dell'apertura con il gesto di scorrimento: quando inizi a scorrere durante l'apertura, il gestore normale dei gesti (`AbsSwipeUpHandler.onLauncherStart`) cancella l'animazione di apertura prima che il sistema proponga il merge. Per questo uno swipe verso l'alto che parte mentre l'app si sta aprendo è gestito da un consumer di input apposta (`LaunchInterruptInputConsumer`), che lo tratta come "vai alla Home". Il sistema offre il merge e la chiusura riparte dal rettangolo in cui si trova la finestra, con la velocità che aveva. Nei log: `Swipe up during an app launch, going home`, poi `Merge accepted: closing … from RectF(…)`. Altri gesti durante i circa 500 ms dell'apertura (scorri e tieni, gesto laterale) vengono ignorati.

Recenti: in Lawnchair `16-dev` `TaskContainer.setState`, `bind`, `destroy` e `refreshOverlay` erano scritte come `fun f() = { … }`, cioè funzioni che restituiscono una lambda senza eseguirla. Per questo le card erano vuote e i pulsanti non facevano nulla. Ora sono funzioni normali. Inoltre `TaskThumbnailCache` prende uno snapshot al volo per il task in primo piano, `SystemUiProxy.takeScreenshot` è di nuovo attivo (era commentato) e `TaskOverlayFactoryImpl` installa i callback di Lawnchair, così **Screenshot** salva lo screenshot di sistema e **Lens** apre Google Lens.

Chiusura con il tasto Home e animazione di Launcher: su PixelOS 17 l'interfaccia `IShellTransitions` ha un metodo in più in testa, quindi tutti i codici di transazione sono quelli di Android 16 più uno (`registerRemote` è 2, `unregisterRemote` è 3). Inoltre `registerRemote` prende solo il `RemoteTransition`, che porta con sé il filtro. Lawnchair mandava la firma di Android 16, la Shell la scartava (`UNKNOWN_TRANSACTION`) e il filtro `QuickstepLaunchHome` non veniva mai registrato: Home faceva la scivolata di sistema ("pull down"). Ora `SystemUiProxy.kt` scrive la transazione a mano. Si verifica con `adb shell dumpsys activity service SystemUIService WMShell`, voce "Registered Remotes", che deve elencare `QuickstepLaunchHome`. Le altre chiamate a `IShellTransitions` fatte con lo stub di Android 16 (listener della Home e del focus, contenitore overlay della Home) finiscono su codici sfalsati e non le ho verificate.

Interruzione fin dal primo frame: l'apertura da icona è interrompibile dal primo frame disegnato, non più dal secondo. Uno scorrimento che parte più di circa 100 ms dopo il tocco sull'icona viene accettato. Se parte prima ancora che la transizione sia iniziata (meno di circa 80 ms dal tocco, un caso che con il dito non capita) l'app resta aperta.

Screenshot e Lens con il gesto "scorri e tieni": `RecentsViewModelHelper.switchToScreenshot` aspettava che il repository delle miniature ricevesse lo snapshot appena scattato, cosa che su Android 17 non succede, e la callback non partiva mai. Ora l'attesa ha un limite di 400 ms. Provato con quattro gesti di fila: Screenshot salva il file in `Pictures/Screenshots` e Lens si apre.

Catena apri, chiudi, riapri senza schermo nero: quando una chiusura prende il posto di un'apertura in corso (merge), il sistema tiene attiva la transizione di Home finché dura l'animazione di chiusura. Se in quel momento tocchi un'icona, la nuova apertura veniva raccolta mentre la vecchia non era ancora conclusa, e Launcher e wallpaper restavano nascosti per tutta l'animazione (schermo nero). Ora al tocco su un'icona l'animazione in corso viene portata subito al suo stato finale e la sua transizione si conclude (`LauncherAnimationRunner.endPlayingAnimation`, chiamata da `getActivityLaunchOptions`). Inoltre, quando il sistema annulla un'animazione, questa salta prima al suo stato finale e solo dopo la transizione viene conclusa (`cancelThenRun`), e il merge mostra esplicitamente le radici e i leash delle finestre che si aprono. Chiusure (Home, Back, swipe) arrivate prima che l'animazione di apertura sia partita restano in coda e partono appena inizia (`closeLaunchingApp`). Provato con apri, chiudi dopo 150 ms e riapri dopo 120 ms fino a 800 ms, con Home, swipe e Back, sulla stessa icona e su un'altra: nessun nero.

## Cose da sapere

- L'APK dentro lo zip è la versione di debug di Lawnchair, con strumenti di diagnostica attivi. Può sembrare meno fluida per motivi che non c'entrano con la mia modifica. Se il modulo parte bene, ricompilo una versione non di debug prima che tu giudichi come si sente l'animazione.
- Non installare dal Play Store o da altre fonti un'altra copia di Lawnchair con una firma diversa: andrebbe in conflitto con quella di sistema.
- L'interruzione dell'apertura da icona funziona con il tasto Home, con Indietro a tasti e con lo scorrimento verso l'alto. Vale per le icone sulla schermata e nel dock. Le aperture da widget e dalle recenti non passano da questo percorso.
- Condividi e Locked nelle recenti sono disattivati nelle impostazioni di Lawnchair; Condividi è implementato, Locked no.
- L'API 17 ha cambiato altre interfacce che Lawnchair non usa nel tuo caso (bolle, PiP, modalità desktop, split). Se un giorno le attivi e crashano, l'elenco è in `backup-phone\aidl-diff-details.txt`, `api-mismatch-a16-vs-a17.txt` e `api-missing-classes-a17.txt`.
- Dopo ogni aggiornamento mensile di PixelOS devi ripatchare il `boot.img` con Magisk.
