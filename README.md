# seven-gooeys
This is an exercise in programming seven different GUIs, as described [here](https://eugenkiss.github.io/7guis/tasks).

## Progress
- [x] 1: Counter
- [x] 2: Temperature Converter
- [x] 3: Flight Booker
- [x] 4: Timer
- [x] 5: CRUD
- [x] 6: Circle Drawer
- [ ] 7: Cells


## Development
To start the Figwheel compiler, navigate to the project folder and run the following command in the terminal:

```
lein figwheel
```

Figwheel will automatically push cljs changes to the browser.
Once Figwheel starts up, you should be able to open the `public/index.html` page in the browser.

## REPL

The project is setup to start nREPL on port `7002` once Figwheel starts.
Once you connect to the nREPL, run `(cljs)` to switch to the ClojureScript REPL.

## Building for production

```
lein clean
lein package
```
