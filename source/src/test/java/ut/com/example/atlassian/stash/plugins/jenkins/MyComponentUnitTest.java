package ut.com.example.atlassian.stash.plugins.jenkins;

import org.junit.Test;
import com.example.atlassian.stash.plugins.jenkins.MyPluginComponent;
import com.example.atlassian.stash.plugins.jenkins.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}