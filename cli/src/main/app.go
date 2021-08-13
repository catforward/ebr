package main

import (
	act "ebr/actions"
	"ebr/symbols"
	utl "ebr/utils"
	"flag"
	"fmt"
	"log"
	"os"
)

var (
	required string
	showCmd  = flag.NewFlagSet(symbols.SHOW, flag.ExitOnError)
	runCmd   = flag.NewFlagSet(symbols.RUN, flag.ExitOnError)
)

var subcommands = map[string]*flag.FlagSet{
	showCmd.Name(): showCmd,
	runCmd.Name():  runCmd,
}

func init() {
	flag.Usage = usage
}

func usage() {
	fmt.Fprintf(os.Stderr, `ebr-cli version: ebr-cli/1.0.0
Usage: ebr [command] [option url] 
Commands:
	show   : show flow's info then exit
	run    : run specified flow then exit
Options:
	-f url : set flow's url
`)
	flag.PrintDefaults()
}

func setupCommonFlags() {
	for _, fs := range subcommands {
		fs.StringVar(&required, "f", "", "required flow's url for all commands")
	}
}

func checkAndRun(flg *flag.FlagSet, action act.IAction) {
	switch flg {
	case showCmd:
		{
			if required == "" {
				action.DoAction(symbols.ALL)
			} else {
				action.DoAction(required)
			}
		}
	case runCmd:
		{
			if required == "" {
				log.Fatalf("-f is required for '%s' command", flg.Name())
				os.Exit(symbols.COMMAND_ERROR)
			}
			action.DoAction(required)
		}
	default:
		log.Fatalf("[ERROR] unknown subcommand '%s', see help for more details.", flg.Name())
		os.Exit(symbols.COMMAND_ERROR)
	}
}

func main() {
	if len(os.Args) < 2 {
		flag.Usage()
		os.Exit(symbols.NORMAL)
	}

	setupCommonFlags()

	cmd := subcommands[os.Args[1]]
	if cmd == nil {
		log.Fatalf("[ERROR] unknown subcommand '%s', see help for more details.", os.Args[1])
		os.Exit(symbols.UNKNOWN_COMMAND)
	}

	cmd.Parse(os.Args[2:])
	action := act.GetAction(cmd.Name())
	if action == nil {
		log.Fatalf("[ERROR] unimplemented action for subcommand '%s'.", os.Args[1])
		os.Exit(symbols.UNIMPLEMENTED_ACTION)
	}

	utl.ConfigInit()
	utl.HttpInit()
	checkAndRun(cmd, action)
}
