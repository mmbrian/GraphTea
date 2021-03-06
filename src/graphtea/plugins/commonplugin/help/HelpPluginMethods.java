// GraphTea Project: http://github.com/graphtheorysoftware/GraphTea
// Copyright (C) 2012 Graph Theory Software Foundation: http://GraphTheorySoftware.com
// Copyright (C) 2008 Mathematical Science Department of Sharif University of Technology
// Distributed under the terms of the GNU General Public License (GPL): http://www.gnu.org/licenses/

package graphtea.plugins.commonplugin.help;

import graphtea.platform.core.BlackBoard;
import graphtea.platform.plugin.PluginMethods;
import graphtea.plugins.commonplugin.help.actions.Help;

import java.net.URL;

/**
 * @author azin azadi
 */
public class HelpPluginMethods implements PluginMethods {
    BlackBoard blackboard;

//todo: REZA,complete this documentation

    public HelpPluginMethods(BlackBoard blackboard) {
        this.blackboard = blackboard;
    }

    /**
     * register som plugin's help contents to be shown on the help of the program
     *
     * @param pluginName name of the plugin
     * @param title      the title to be shown on the screen
     * @param filter     this filter determines a path to search for files of the help
     * @return the path to the extracted indexXXXXXX.html of the plugin
     */
    public URL registerHelpPlugin(String pluginName, String title, String filter) {
        return Utils.registerHelpPlugin(blackboard, pluginName, title, filter);
    }

    public void showHelpWindow() {
        Help.showHelpWindow();
    }
}
