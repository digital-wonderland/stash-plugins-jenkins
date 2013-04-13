node default {

  package { [ 'git' ]:
    ensure => installed
  }

  yumrepo { 'netcentric':
    include => 'http://obs.int.netcentric.biz:82/netcentric/CentOS_6/netcentric.repo'
  }

  package { 'oracle-7-jdk':
    ensure  => 'installed',
    require => Yumrepo['netcentric']
  }

  yumrepo { 'jenkins':
    include => 'http://pkg.jenkins-ci.org/redhat/jenkins.repo',
    gpgkey  => 'http://pkg.jenkins-ci.org/redhat/jenkins-ci.org.key'
  }

  package { 'jenkins':
    ensure => 'installed',
    require => Yumrepo['jenkins']
  }

  service { 'jenkins':
    ensure  => 'running',
    enable  => true,
    require => [ Package['jenkins'], Package['oracle-7-jdk'] ]
  }

}
