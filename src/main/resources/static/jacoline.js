function clearTextArea() {
    document.getElementById('form_ta').innerHTML = '';
}

function showExample() {

    var example = '';

    example += '/usr/bin/java -Dfoo=bar ';
    example += '-cp=/all/the/jars ';
    example += '-java.library.path=/home/chris/lib ';
    example += '-Xmx8g ';
    example += '-XX:+UseG1GC ';
    example += '-XX:MaxGCPauseMillis=100 ';
    example += '-XX:+UnlockDiagnosticVMOptions ';
    example += '-XX:+BADSWITCH ';
    example += '-XX:+LogCompilation ';
    example += '-XX:LogFile=&#34;/home/chris/hotspot.log&#34; ';
    example += '-XX:FreqInlineSize=512 ';
    example += '-Xmx4g ';
    example += '-XX:+TraceRangeCheckElimination ';
    example += '-XX:+UnsyncloadClass ';
    example += '-XX:+UseSpinning ';
    example += '-XX:+PrintCodeHeapAnalytics ';
    example += '-XX:ReplaySuppressInitializers=4 ';
    example += 'com.chrisnewland.someproject.SomeApplication';

    example = example.replace(/-/g, "&#8209;"); //

    document.getElementById('form_ta').innerHTML = example;
}
