package ru.snowleos.jenkins.builddescription;
import hudson.Extension;
import hudson.Launcher;
import hudson.FilePath;
import hudson.tasks.BuildStepMonitor;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Action;
import hudson.model.ProminentProjectAction;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import javax.servlet.ServletException;
import org.kohsuke.stapler.QueryParameter;

public class BuildDescriptionPublisher extends Notifier {

    private final String name;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public BuildDescriptionPublisher(String name) {
        this.name = name;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        if(name.isEmpty()){
            listener.getLogger().println("Build description: filename is not set.");
            return true;
        }
        try {
            String data = 
                    (new FilePath(build.getWorkspace(), name)
                    .readToString());
            build.setDescription(data);
        } catch (IOException ex) {
            listener.getLogger().println("Build description: could not read data file: "+ex.getLocalizedMessage());
        }
        return true;
    }


    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public static final class LinkAction implements Action, ProminentProjectAction{
        private final String name;
        private final String url;
        private final String icon;

        public LinkAction(String [] params){
            this.name = params[0];
            this.url = params[1];
            if(params.length < 3) 
            {
                this.icon = "graph.gif";
            }
            else this.icon = params[2];
        }
        public String getIconFileName() {
            return icon;
        }


        public String getDisplayName() {
            return name;
        }

        public String getUrlName() {
            return url;
        }

    }    
    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }
        @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {


        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a filename");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }
        
        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Build Description";
        }

    }
 
}

