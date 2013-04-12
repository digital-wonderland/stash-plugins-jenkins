Vagrant::Config.run do |config|
  
  config.vm.box = "CentOS-6.4"
  config.vm.box_url = "http://logs.int.netcentric.biz/CentOS-6.4.box"
  
  config.vm.network(:hostonly, "33.33.33.89")
  config.vm.host_name = "stash-plugins-jenkins"
    
  config.vm.share_folder "m2", "/home/vagrant/.m2", "~/.m2"
    
  config.vm.customize ["modifyvm", :id,
    	"--name", "stash-plugins-jenkins"
    ]

  config.vm.forward_port 5005, 5005
  config.vm.forward_port 7990, 7990

  #config.vm.provision :puppet do |puppet|
  #  puppet.manifests_path = "config"
  #  puppet.manifest_file = "puppet.pp"
  #end
end

