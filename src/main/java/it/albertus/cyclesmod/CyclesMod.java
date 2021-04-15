package it.albertus.cyclesmod;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.Callable;

import it.albertus.cyclesmod.cli.CyclesModCli;
import it.albertus.cyclesmod.cli.VersionProvider;
import it.albertus.cyclesmod.gui.CyclesModGui;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Parameters;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Command(versionProvider = VersionProvider.class, mixinStandardHelpOptions = true, resourceBundle = "it.albertus.cyclesmod.cli.resources.picocli")
public class CyclesMod implements Callable<Integer> {

	@Parameters(arity = "0..1", descriptionKey = "parameter.path") private Path path;

	public static void main(final String... args) {
		System.exit(new CommandLine(new CyclesMod()).setCommandName(CyclesMod.class.getSimpleName().toLowerCase(Locale.ROOT)).setOptionsCaseInsensitive(true).execute(args));
	}

	@Override
	public Integer call() {
		final String mode = System.getProperty(getClass().getName() + ".main.mode");
		if (mode != null) {
			if ("cli".equalsIgnoreCase(mode)) {
				CyclesModCli.main(path);
			}
			else if ("gui".equalsIgnoreCase(mode)) {
				CyclesModGui.main(path);
			}
		}
		else {
			if (path != null && Files.isDirectory(path)) {
				CyclesModCli.main(path);
			}
			else {
				CyclesModGui.main(path);
			}
		}
		return ExitCode.OK;
	}

}
