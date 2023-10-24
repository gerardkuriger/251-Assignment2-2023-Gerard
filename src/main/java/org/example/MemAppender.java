package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@Plugin(
        name = "MemAppender",
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE
)
public class MemAppender extends AbstractAppender {
    static class RingBuffer{
        private int maxSize = 256;
        private LogEvent[] CurrentLogs;
        int startPointer = 0;
        int endPointer = 0;
        RingBuffer(){
            CurrentLogs = new LogEvent[maxSize];
        }
        RingBuffer( int capacity ){
            this.maxSize = (capacity < 1) ? maxSize : capacity;
            CurrentLogs = new LogEvent[maxSize];
        }
        private boolean isFull() {
            return ( (startPointer == 0 && endPointer == maxSize) || (startPointer > 1 && endPointer == startPointer) );
        }

        /**
         * @param event is added into the Ring Buffer that stores a Limited number of Logging Events
         */
        public void add(LogEvent event) {
            endPointer++;
            if ( isFull() ) {
                startPointer++;
                if (endPointer == maxSize){
                    endPointer=0;
                } else if (startPointer == maxSize) {
                    startPointer=0;
                }
            } else {
                if (endPointer == maxSize){
                    endPointer=0;
                }
            }
            CurrentLogs[endPointer] = event;
        }

        /**
         * @return a reformatted Array containing the logging events from latest (result[0]) to most recent (result[-1])
         */
        public LogEvent[] getEvents(){
            LogEvent[] result = new LogEvent[getCurrentSize()];
            int j=0;
            for (int i=startPointer; i==endPointer; i++){
                if (i==maxSize) i=0;
                result[j] = CurrentLogs[i];
                j++;
            }
            return result;
        }
        public int getMaxSize() {
            return maxSize;
        }
        public void setMaxSize(int maxSize) {
            LogEvent[] oldEvents = getEvents();
            CurrentLogs = new LogEvent[maxSize];
            int j=0;
            for (int i = startPointer; i==endPointer; i++){
                if (i == this.maxSize) i=0;
                CurrentLogs[j] = oldEvents[i];
            }
            endPointer=getCurrentSize();
            startPointer=0;
            this.maxSize = maxSize;
        }
        public int getCurrentSize() {
            if (startPointer > endPointer) {
                return endPointer+maxSize-startPointer;
            }
            return endPointer-startPointer;
        }
    }

    RingBuffer Logs = new RingBuffer();

    /**
     * Constructor.
     *
     * @param name             The Appender name.
     * @param filter           The Filter to associate with the Appender.
     * @param layout           The layout to use to format the event.
     * @param ignoreExceptions If true, exceptions will be logged and suppressed. If false errors will be logged and
     *                         then passed to the application.
     * @param properties       Any other properties that this Appender contains
     */
    protected MemAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
    }

    @PluginFactory
    public static @NotNull MemAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter,
            @PluginElement("Layout") Layout<Serializable> layout,
            @PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
            @PluginElement("Properties") Property[] properties ) {
        return new MemAppender(name, filter, layout, ignoreExceptions, properties);
    }

    @Override
    public void append(LogEvent event) {
        Logs.add(event);
    }

    /**
     * @return an unmodified list of the LoggingEvents
     */
    public LogEvent[] getCurrentLogs() { return Logs.getEvents(); }
    public String[] getEventStrings() {
        String[] result = new String[Logs.getCurrentSize()];
        LogEvent[] currentLogs = Logs.getEvents();
        Layout<? extends Serializable> layout = getLayout();


        return result;
    }
}
