package com.freedom.asyncimageloader;


    /**
     * Indicates the current status of the task. Each status will be set
     * during process.
     */
    public enum TaskStatus {
        /**
         * Indicates that the task is not yet started.
         */
        PENDING,
        /**
         * Indicates that the task is running.
         */
        RUNNING,
        /**
         * Indicates that the task is pause.
         */
        PAUSE,
        /**
         * Indicates that loading task has finished loading.
         */
        FINISHED,
    }
    
