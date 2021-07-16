package main

import (
	"flag"
	"fmt"
	"log"
	"os"
	"ebr/act"
)

var (
	required string
	showCmd = flag.NewFlagSet("show", flag.ExitOnError)
	runCmd = flag.NewFlagSet("run", flag.ExitOnError)
)

var subcommands = map[string] *flag.FlagSet{
    showCmd.Name(): showCmd,
    runCmd.Name(): runCmd,
}

func init() {
	flag.Usage = usage
}

func usage() {
	fmt.Fprintf(os.Stderr, `ebr-cli version: ebr-cli/1.0.0
Usage:
		ebr <command> [arguments] target
Command:
		show
		run
Argument:
		t
Sample:
		ebr show -t /FLOW-1
`)
	flag.PrintDefaults()
}

func setupCommonFlags() {
    for _, fs := range subcommands {
        fs.StringVar(
            &required,
            "t",
            "",
            "required task url for all commands",
        )
    }
}

func checkAndRun(flg *flag.FlagSet, action act.IAction) {
	switch flg {
	case showCmd: {
		if required == "" {
			action.DoAction("all")
		} else {
			action.DoAction(required)
		}
	}
	case runCmd: {
		if required == "" {
			log.Fatalf("-t is required for '%s' command", flg.Name())
			os.Exit(3)
		}
		action.DoAction(required)
	}
	default:
		log.Fatalf("[ERROR] unknown subcommand '%s', see help for more details.", flg.Name())
		os.Exit(3)
	}
}

func main() {
	if len(os.Args) < 2 {
		flag.Usage()
		os.Exit(0)
	}

	setupCommonFlags()

    cmd := subcommands[os.Args[1]]
    if cmd == nil {
        log.Fatalf("[ERROR] unknown subcommand '%s', see help for more details.", os.Args[1])
		os.Exit(1)
    }

	cmd.Parse(os.Args[2:])
	action := act.GetAction(cmd.Name())
	if action == nil {
		log.Fatalf("[ERROR] unimplemented action for subcommand '%s'.", os.Args[1])
		os.Exit(2)
	}
	checkAndRun(cmd, action)
}