# Deploy su Render (piano free)

Il progetto viene pubblicato come **un unico servizio Docker**: il frontend
Angular viene compilato e copiato dentro il jar di Spring Boot, che serve sia
le pagine sia le API. In questo modo:

- non ci sono problemi di CORS;
- si consuma **una sola** istanza free (le 750 ore mensili sono condivise da
  tutti i servizi free del workspace);
- c'è un solo URL da ricordare.

File aggiunti per il deploy:

| File | Ruolo |
| --- | --- |
| `Dockerfile` | compila Angular, poi Spring Boot, e crea l'immagine finale |
| `.dockerignore` | esclude `node_modules`, `build`, `dist`, `.git` dal build |
| `render.yaml` | Blueprint Render: web service + PostgreSQL free |
| `environment.production.ts` | in produzione le API sono su `/api` (stesso dominio) |
| `DataSourceConfig.java` | accetta l'URL PostgreSQL fornito da Render |
| `SpaFallbackFilter.java` | refresh delle rotte Angular (es. `/televisori/12`) |

## 1. Mettere il codice su GitHub

Il repository locale esiste già ma non ha ancora commit. Da `C:\Progetti\Gestionale calibrazione`:

```powershell
git add .
git commit -m "Prima versione"
git branch -M main
```

Creare un repository vuoto su <https://github.com/new> (va bene anche privato),
poi:

```powershell
git remote add origin https://github.com/TUO_UTENTE/gestionale-calibrazione.git
git push -u origin main
```

Prima del commit controllare che `git status` non mostri `node_modules`,
`dist` o `build`: sono già esclusi dal `.gitignore`.

## 2. Creare il Blueprint su Render

1. Accedere a <https://dashboard.render.com> con GitHub.
2. **New +** → **Blueprint**.
3. Collegare GitHub e scegliere il repository appena creato.
4. Render legge `render.yaml` e mostra:
   - il web service `gestionale-calibrazione` (piano free, Docker);
   - il database `calibrazione-db` (PostgreSQL 17, piano free).
5. Quando viene chiesto **APP_ADMIN_PASSWORD**, scegliere una password per
   l'amministratore (l'email è `admin@mediaworld.it`).
6. **Apply** e attendere il primo deploy (circa 5–10 minuti: va scaricato
   Gradle e compilato Angular).

Alla fine Render mostra un URL tipo
`https://gestionale-calibrazione.onrender.com`.

## 3. Primo accesso

- Email: `admin@mediaworld.it`
- Password: quella scelta al punto 2.

Il database viene creato automaticamente e `schema.sql` è idempotente. Il
primo avvio crea solo l'amministratore, senza dati di esempio.

Per caricare i dati demo (5 utenti e 20 televisori) impostare
`APP_SEED_DEMO=true` nelle variabili d'ambiente e rifare il deploy.
Attenzione: i dati demo comprendono l'account `admin@mediaworld.it` con
password nota `Admin123!` e ruolo amministratore. Vanno bene per una demo
temporanea, non per un URL pubblico con dati reali.

## 4. Limiti del piano free (importante)

- Il servizio **va in pausa dopo 15 minuti** senza traffico: la prima visita
  successiva richiede circa un minuto.
- RAM: **512 MB** (bastano per questa applicazione; il Dockerfile limita già
  la JVM al 70%).
- Il **PostgreSQL free scade dopo 30 giorni** dalla creazione (poi 14 giorni
  di tolleranza prima della cancellazione). Per un uso continuativo conviene
  passare a un database free che non scade, ad esempio **Neon**.
- Non è possibile allegare dischi persistenti ai servizi free: i dati devono
  stare nel database (come in questo caso).

### Passare a Neon (facoltativo, dati permanenti)

1. Creare un progetto gratuito su <https://neon.tech>.
2. Copiare la **connection string** (formato `postgresql://...` oppure
   `jdbc:postgresql://...`).
3. Su Render → servizio → **Environment** → aggiungere/modificare `DB_URL`
   con quella stringa e salvare.
4. Render rifà il deploy: lo schema viene ricreato e l'amministratore
   reinserito. I dati del vecchio database non vengono copiati
   automaticamente (servirebbe un `pg_dump`/`pg_restore`).

`DataSourceConfig` accetta sia `postgresql://utente:password@host/db` sia
`jdbc:postgresql://host/db`, quindi non serve riscrivere l'URL a mano.

### Modalità demo senza database

Se serve solo mostrare l'interfaccia, si può impostare:

```
SPRING_PROFILES_ACTIVE=mock
```

L'applicazione usa il repository in memoria (nessun database). I dati si
azzerano a ogni riavvio e il servizio può restare sul piano free anche dopo
i 30 giorni del PostgreSQL.

## 5. Test locale dell'immagine (facoltativo)

Con Docker Desktop installato:

```powershell
docker build -t gestionale-calibrazione .
docker run --rm -p 8080:8080 -e PORT=8080 gestionale-calibrazione
```

Aprire <http://localhost:8080>. Senza variabili aggiuntive parte in profilo
`mock`, quindi non serve un database.

## 6. Aggiornare l'applicazione

Ogni `git push` su `main` fa ripartire automaticamente il deploy su Render
(`autoDeployTrigger: commit` è il comportamento predefinito dei Blueprint).

Se il build fallisce, il log su Render indica il passaggio esatto: i due punti
più comuni sono `npm ci` (lockfile non aggiornato) e `./gradlew bootJar`
(errore di compilazione Java).
