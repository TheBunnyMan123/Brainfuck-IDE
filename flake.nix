{
    description = "Development flake for TheKillerBunny's Brainfuck IDE";
    inputs = {
        nixpkgs.url = "github:nixos/nixpkgs?ref=nixos-unstable";
        flake-utils = {
            url = "github:numtide/flake-utils";
            inputs = {
                nixpkgs.follows = "nixpkgs";
            };
        };
    };
    outputs = { self, nixpkgs, flake-utils }: flake-utils.lib.eachDefaultSystem (system: let
      pkgs = nixpkgs.legacyPackages."${system}";
    in {
        devShells = {
            default = pkgs.mkShellNoCC {
                packages = with pkgs; [
                    jetbrains.idea-community-bin
                    jdk21
                    maven
                ];
            };
        };
    });
}
