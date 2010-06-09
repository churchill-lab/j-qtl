#!/usr/jax/bin/perl
######################################################################
#
#    license.pl
#
#    Hao Wu 
#    April, 2002
#
#    This program takes a data from a software registration form,
#    writes it to a file, and presents a webpage allowing download
#    of the requested software.
#
######################################################################

# Script located at:
# aretha.jax.org:/data/htdocs/jax/jax-cgi/churchill/download/license.pl

# file to which information should be added.
$file = "../log/download";
$maillist = "../log/maillist";

# software names etc
@software = ("maanova","pm", "Rmaanova", "Jmaanova", "Jqtl", "Blitzkrieg", "PubArray");

# get time of submission
($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
$mon++; #$mday++; 
while($year >= 100) {
    $year -= 100;
}
if($mon < 10) { $mon = "0$mon"; }
if($mday < 10) { $mday = "0$mday"; }
if($year < 10) { $year = "0$year"; }
if($hour < 10) { $hour = "0$hour"; }
if($min < 10) { $min = "0$min"; }
if($sec < 10) { $sec = "0$sec"; }

# read in form information
read(STDIN, $buffer, $ENV{'CONTENT_LENGTH'});
# write buffer to debug file
open(OUT, "debug");
print OUT "$buffer";
close (OUT);

@pairs = split(/&/, $buffer);
foreach $pair (@pairs) {
    ($name, $value) = split(/=/, $pair);
    # parse information
    $value =~ s/%([a-fA-F0-9][a-fA-F0-9])/pack("C", hex($1))/eg;
    # replace + with spaces
    $value =~ s/\+/ /g;
    # remove extraneous spaces
    @x = split(/\s+/, $value);
    $value = join(" ", @x);
    $FORM{$name} = $value;
}
close(STDIN);

# extract parameters
$accept = $FORM{'accept'};
$name = $FORM{'name'};
$institution = $FORM{'institution'};
$email = $FORM{'email'};
$tel = $FORM{'telephone'};
$os = lc($FORM{'os'});
$updates = $FORM{'updates'};

# check the user input information
$nerror = 0;
if($accept ne "yes") {
    $errormsg[$nerror] = "Please accept the warranty disclaimer and Copyright Notice";
    $nerror ++;
}
if($name eq "") {
    $errormsg[$nerror] = "Please enter your name";
    $nerror ++;
}
if($institution eq "") {
    $errormsg[$nerror] = "Please enter your institution";
    $nerror ++;
}
if($email eq "") {
    $errormsg[$nerror] = "Please enter your email address";
    $nerror ++;
}

$ndownload = 0;
foreach $i (0..(@software-1)) {
    if($FORM{$software[$i]} eq "on") {
	$download[$ndownload] = $software[$i];
	$ndownload ++;
    }
}
if($ndownload eq 0) {
    $errormsg[$nerror] = "Please select a software to download";
    $nerror ++;
}    

# display the common content of the page
print "Content-type: text/html\n\n";
print "<HTML>\n";
# header info
open(HEADER, "../head.txt" );
print <HEADER>;
# if there's no error, display the download page and write to log file
if($nerror eq 0) { 
    &log;
    &downpage;
}
else { #if there's any error, display the error message
    &errorpage;
}

print <<ENDOF;
</HTML>

ENDOF

######################### finish of the main program #############################

sub log{ # sub function to write the registration information to a log file

  if(!open(OUT, ">>$file")) {
    $didntwork = 1;
  }
  else {
    print OUT ("$mon/$mday/$year $hour:$min:$sec\n");
    print OUT ("     $name; $email; $institution; $tel; $os; $updates\n");
    print OUT ("     Download: ");
    foreach $s (@download) {
	print OUT "$s ";
    }
    print OUT ("\n\n");
    close(OUT);
  }
  # if update is yes, write the email address to maillist
  if(!open(OUT, ">>$maillist")) {
     $didntwork = 1;
  }
  else {
      if($updates eq "yes") {
	  print OUT ("$email,\n"); }
  }
}


sub downpage { # display the download page 

print "<H3>Software Download</H3>";
print "<P>Thank you! Click the following link(s) to download the software.";

$i = 0;
foreach $s (@download) {
    if ($s eq "maanova") { # user want to download maanova
	$text = "MAANOVA";
	if(substr($os,0,3) eq "win") { #user's OS is windows
	    $filename[$i] = "anova/maanova_2_0_win.zip";
	    $text = $text . " (Windows version)";
	}
	else { #user's OS is Linux/Unix or mac
	    $filename[$i] = "anova/maanova_2_0_linux.tar.gz";
	    $text = $text . " (Linux/Unix version)";
	}
    }
    elsif($s eq "pm") { # user want to download pseudo marker
	$text = "PseudoMarker";
        $filename[$i] = "pseudomarker/pm204.zip";
    }
    elsif($s eq "PubArray") { # user want to download PubArray
	$text = "PubArray Installer";
        $filename[$i] = "pub-array/1.0.0/app/pub-array.jnlp";
    }
    elsif($s eq "Blitzkrieg") { # user want to download Blitzkrieg
	$text = "Blitzkrieg Installer";
        $filename[$i] = "blitzkrieg/1.0.0/app/blitzkrieg.jnlp";
    }
	
    if($s eq "Jqtl") { #user want to download J/qtl
		if(substr($os,0,3) eq "win" || substr($os,0,3) eq "mac")
		{
			print "<P><A HREF=http://cgd.jax.org/churchill-apps/jqtl-1.3.1/app/j-qtl.jnlp>J/qtl Installer</A> (Recommended): " .
			      "Installer launches when clicked. Please grant J/qtl execution privileges when prompted during the installation. Also, it is " .
				  "recommended that you allow the installer to create shortcuts so that you can restart the application when offline.<BR>";
			print "<P><A HREF=http://cgd.jax.org/churchill-apps/jqtl-1.3.1/app/dist/j-qtl-1.3.1.zip>J/qtl Stand-Alone Binaries</A>: " .
			      " Download this version if you do not want to run the installer.";
			print "<P><A HREF=http://cgd.jax.org/churchill-apps/jqtl-1.3.1/j-qtl-1.3.1-src.tgz>J/qtl Source Code</A>: " .
			      " Download this version if you want the GPL v3 source code (only for programmers).";
		}
		else
		{
			print "<P>Sorry, J/qtl is not yet available for Linux or Unix. Support for Linux and possibly Unix will be available in the next major release.<BR>";
		}
    }
    elsif($s eq "Jmaanova") { #user want to download J/maanova
		if(substr($os,0,3) eq "win" || substr($os,0,3) eq "mac")
		{
			print "<P><A HREF=http://cgd.jax.org/churchill-apps/jmaanova-1.0.0/app/j-maanova.jnlp>J/maanova Installer</A> (Recommended): " .
			      "Installer launches when clicked. Please grant J/maanova execution privileges when prompted during the installation. Also, it is " .
				  "recommended that you allow the installer to create shortcuts so that you can restart the application when offline.<BR>";
			print "<P><A HREF=http://cgd.jax.org/churchill-apps/jmaanova-1.0.0/j-maanova-1.0.0-src.tgz>J/maanova Source Code</A>: " .
			      " Download this version if you want the GPL v3 source code (only for programmers).";
		}
		else
		{
			print "<P>Sorry, J/maanova is not yet available for Linux or Unix. Support for Linux and possibly Unix will be available in the next major release.<BR>";
		}
    }
    elsif($s eq "Rmaanova") { #user want to download R/maanova
        $text = "R/maanova";
        if(substr($os,0,3) eq "win") { #user's OS is windows
            $filename[$i] = "rmaanova/maanova_1.16.0.zip";
            $text = $text . " (Windows binary version)";
        }
        elsif (substr($os,0,3) eq "mac") { # user's OS is Mac OSX
	    $filename[$i] = "rmaanova/maanova_1.16.0.tar.gz";
	    $text = $text . " (Mac OS X version)";
	}
	else { #user's OS is Linux/Unix
            $filename[$i] = "rmaanova/maanova_1.16.0.tar.gz";
            $text = $text . " (Linux/Unix version)";
        }

        print "<P><A HREF=http://cgd.jax.org/churchill-apps/$filename[$i]>$text</A><BR>";
    }
	else {
    print "<P><A HREF=http://cgd.jax.org/churchill-apps/$filename[$i]>$text</A><BR>";
	}
	
    $i ++;
}
}

sub errorpage { # display the error msgs

print "<P>There are following errors in your registration:";
print "<UL>";
foreach $i (0..($nerror-1)) {
    print "<LI>$errormsg[$i]</LI>";
}
print "</UL>";
print "<P>Click Back button in your browser to make the changes and submit again!";
}    

