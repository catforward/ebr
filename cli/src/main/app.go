package main

import (
	act "ebr/actions"
	"ebr/consts"
	req "ebr/net"
	utl "ebr/utils"
	"flag"
	"fmt"
	"log"
	"os"
)

var (
	required string
	showCmd  = flag.NewFlagSet(consts.SHOW, flag.ExitOnError)
	runCmd   = flag.NewFlagSet(consts.RUN, flag.ExitOnError)
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
	show   : show task's info then exit
	run    : run specified task then exit
Options:
	-t url : set target task's url
`)
	flag.PrintDefaults()
}

func setupCommonFlags() {
	for _, fs := range subcommands {
		fs.StringVar(&required, "t", "", "required task url for all commands")
	}
}

func checkAndRun(flg *flag.FlagSet, action act.IAction) {
	switch flg {
	case showCmd:
		{
			if required == "" {
				action.DoAction(consts.ALL)
			} else {
				action.DoAction(required)
			}
		}
	case runCmd:
		{
			if required == "" {
				log.Fatalf("-t is required for '%s' command", flg.Name())
				os.Exit(consts.COMMAND_ERROR)
			}
			action.DoAction(required)
		}
	default:
		log.Fatalf("[ERROR] unknown subcommand '%s', see help for more details.", flg.Name())
		os.Exit(consts.COMMAND_ERROR)
	}
}

func main() {
	if len(os.Args) < 2 {
		flag.Usage()
		os.Exit(consts.NORMAL)
	}

	setupCommonFlags()

	cmd := subcommands[os.Args[1]]
	if cmd == nil {
		log.Fatalf("[ERROR] unknown subcommand '%s', see help for more details.", os.Args[1])
		os.Exit(consts.UNKNOWN_COMMAND)
	}

	cmd.Parse(os.Args[2:])
	action := act.GetAction(cmd.Name())
	if action == nil {
		log.Fatalf("[ERROR] unimplemented action for subcommand '%s'.", os.Args[1])
		os.Exit(consts.UNIMPLEMENTED_ACTION)
	}

	utl.ConfigInit()
	req.HttpInit()
	checkAndRun(cmd, action)
}
