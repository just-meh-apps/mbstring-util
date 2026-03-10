# To learn more about how to use Nix to configure your environment
# see: https://developers.google.com/idx/guides/customize-idx-env
{ pkgs, ... }: {
  # Which nixpkgs channel to use.
  channel = "stable-24.05"; # or "unstable"
  # Use https://search.nixos.org/packages to find packages
  packages = [
    pkgs.jdk8 # For Java 8 development
    pkgs.maven # For building the project
  ];
  # Sets environment variables in the workspace
  env = {};
  idx = {
    # Search for the extensions you want on https://open-vsx.org/ and use "publisher.id"
    extensions = [
      "vscjava.vscode-java-pack" # Recommended extensions for Java development
    ];
    # Enable previews
    previews = {
      enable = false; # Disabled for now, can be enabled for web apps
    };
    # Workspace lifecycle hooks
    workspace = {
      # Runs when a workspace is first created
      onCreate = {
        # Example: build the project
        # build = "mvn package";
      };
      # Runs when the workspace is (re)started
      onStart = {
        # Example: run the application
        # run-app = "java -cp target/my-app-1.0-SNAPSHOT.jar com.example.App";
      };
    };
  };
}