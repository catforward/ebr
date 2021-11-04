package main

/*
作为EBR的命令行前端，通过子命令（Action）向API Server发起HTTP请求
将响应数据显示到终端
*/
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
	requiredFlow string
	showCmd      = flag.NewFlagSet(symbols.SHOW, flag.ExitOnError)
	startCmd     = flag.NewFlagSet(symbols.START, flag.ExitOnError)
	abortCmd     = flag.NewFlagSet(symbols.ABORT, flag.ExitOnError)
)

var subcommands = map[string]*flag.FlagSet{
	showCmd.Name():  showCmd,
	startCmd.Name(): startCmd,
	abortCmd.Name(): abortCmd,
}

func init() {
	flag.Usage = usage
}

func usage() {
	fmt.Fprintf(os.Stderr, `ebr-cli version: ebr-cli/1.0.0
Usage: ebr [command] [option url] 
Commands:
	show   : show flow's info then exit
	start  : run the specified flow then exit
	abort  : try to abort the specified flow then exit
Options:
	-f url : set flow's url
`)
	flag.PrintDefaults()
}

func setupCommonFlags() {
	for _, fs := range subcommands {
		fs.StringVar(&requiredFlow, "f", "", "required flow's url for all commands")
	}
}

func checkAndRun(flg *flag.FlagSet, action act.IAction) {
	switch flg {
	case showCmd:
		{
			if requiredFlow == "" {
				action.DoAction(symbols.ALL)
			} else {
				action.DoAction(requiredFlow)
			}
		}
	case startCmd:
		{
			if requiredFlow == "" {
				log.Fatalf("-f is required for '%s' command", flg.Name())
				os.Exit(symbols.COMMAND_ERROR)
			}
			action.DoAction(requiredFlow)
		}
	case abortCmd:
		{
			if requiredFlow == "" {
				log.Fatalf("-f is required for '%s' command", flg.Name())
				os.Exit(symbols.COMMAND_ERROR)
			}
			action.DoAction(requiredFlow)
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
