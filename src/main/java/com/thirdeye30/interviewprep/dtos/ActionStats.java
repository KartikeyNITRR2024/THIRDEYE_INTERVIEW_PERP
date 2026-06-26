package com.thirdeye30.interviewprep.dtos;

import java.util.concurrent.atomic.LongAdder;

public class ActionStats {
	public LongAdder views = new LongAdder();
    public LongAdder downloads = new LongAdder();
    public LongAdder addFolders = new LongAdder();
    public LongAdder addFiles = new LongAdder();
}
