# -*- mode: ruby -*-
# vi: set ft=ruby :

# This is a Vagrant file (https://docs.vagrantup.com/v2/).  Use this
# to get a standard development environment for the ABS tools.

# To get started, install vagrant
# (https://www.vagrantup.com/downloads.html) and VirtualBox
# (https://www.virtualbox.org/wiki/Downloads).  Then, from this
# directory, run "vagrant up".  When run the first time, this command
# will download and install an ABS environment; subsequent invocations
# will be much faster.

# To use the tools, execute "vagrant up" then "vagrant ssh".

# For running graphical programs from inside the VM (eclipse,
# key-abs), you will need an X server installed: XQuartz
# (http://xquartz.macosforge.org) for OS X or Xming
# (http://sourceforge.net/projects/xming/) for Windows.

# If you want to modify the installed software, edit the
# "config.vm.provision" at the end of this file.

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  # All Vagrant configuration is done here.  For a complete reference,
  # please see the online documentation at
  # https://docs.vagrantup.com/v2/
  
  config.vm.network "public_network"

  config.vm.box = "ubuntu/trusty64"

  config.vm.post_up_message = <<-MSG
Welcome to the ABS toolchain VM.
The following tools are available from the command line:

- absc             command-line ABS compiler
- eclipse          Eclipse Luna with plugins for ABS, SACO,
                   deadlock analysis pre-installed
- key-abs          Deductive verification tool
- emacs            Emacs, configured to edit and compile ABS
- costabs_exe      Command-line interface to SACO

On Windows / Mac OS X, start an X server (Xming / XQuartz)
MSG

  config.ssh.forward_x11 = true

  config.vm.provider "virtualbox" do |vb|
    vb.memory = 4096
    vb.cpus = 2
    vb.name = "ABS tools VM (Vagrant)"
  end

  # Install necessary software
  config.vm.provision "shell",
                      privileged: false,
                      inline: <<-SHELL

echo
echo "Installing system updates"
echo
sudo apt-get update -y -q
sudo apt-get dist-upgrade -y

echo
echo "Preparing system for Erlang R17.  See"
echo "https://www.erlang-solutions.com/downloads/download-erlang-otp#tabs-ubuntu"
echo
wget -q http://packages.erlang-solutions.com/erlang-solutions_1.0_all.deb
sudo dpkg -i erlang-solutions_1.0_all.deb
rm erlang-solutions_1.0_all.deb

echo
echo "Preparing system for Java 8 (needed by key-abs).  See"
echo "https://gist.github.com/tinkerware/cf0c47bb69bf42c2d740"
echo
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get -y -q update
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true \
   | sudo /usr/bin/debconf-set-selections

echo
echo "Installing necessary tools for the ABS compiler"
echo
sudo apt-get -y -q install software-properties-common htop
sudo apt-get -y -q install oracle-java8-installer
sudo apt-get install -y -q ant antlr junit git unzip
sudo apt-get install -y -q erlang
sudo update-java-alternatives -s java-8-oracle

echo
echo "Installing necessary tools for simulating ABS programs"
echo
sudo apt-get install -y -q emacs maude

echo
echo "Installing eclipse"
echo
sudo apt-get install -y -q eclipse graphviz

echo
echo "Building the ABS compiler and eclipse plugins"
echo
(cd /vagrant/eclipse-plugin ; ant -Declipse.home=/usr build-all-plugins generate-update-site)

echo
echo "Deploying to eclipse"
echo
eclipse -application org.eclipse.equinox.p2.director -noSplash \
        -repository \
file:/vagrant/eclipse-plugin/update-site,\
http://download.eclipse.org/releases/indigo/ \
-installIUs \
org.abs-models.costabs.feature.group,\
org.abs-models.apet.feature.group,\
org.abs-models.abs.compiler.feature.group,\
org.abs-models.sda.feature.group,\
org.abs-models.abs.plugin,\
org.abs-models.sdedit.feature.group


echo
echo "Installing KeY-ABS"
echo
wget -q http://www.key-project.org/key-abs/key-abs.zip
(cd /usr/local/lib && sudo unzip -o /home/vagrant/key-abs.zip)
rm key-abs.zip
cat >key-abs <<EOF
#!/bin/sh
java -jar /usr/local/lib/key-abs/key.jar "\\$@"
EOF
sudo mv key-abs /usr/local/bin
sudo chown root.root /usr/local/bin/key-abs
sudo chmod a+x /usr/local/bin/key-abs
# work around bug in key-abs: it doesn't create the directory it requires
mkdir -p /home/vagrant/.key

echo
echo "Setting up the user environment: .bashrc, .emacs"
echo

# Set up Emacs
if [ ! -e /home/vagrant/.emacs ] ; then
cat >/home/vagrant/.emacs <<EOF
;; Set up ABS, Maude.  Added by Vagrant provisioning
(add-to-list 'load-path "/vagrant/emacs")
(autoload 'abs-mode "abs-mode" "Major mode for editing Abs files." t)
(add-to-list 'auto-mode-alist (cons "\\\\.abs\\\\'" 'abs-mode))
(autoload 'maude-mode "maude-mode" nil t)
(autoload 'run-maude "maude-mode" nil t)
(add-to-list 'auto-mode-alist '("\\\\.maude\\\\'" maude-mode))
EOF
fi

# Set up paths
cat >/home/vagrant/.abstoolsrc <<EOF
PATH=\$PATH:/vagrant/frontend/bin/bash:/vagrant/costabs-plugin
EOF

if [ -z "$(grep abstoolsrc /home/vagrant/.bashrc)" ] ; then
cat >>/home/vagrant/.bashrc <<EOF
. .abstoolsrc
EOF
fi

  SHELL
end
