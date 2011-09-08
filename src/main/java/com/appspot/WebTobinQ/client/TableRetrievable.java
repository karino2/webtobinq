package com.appspot.WebTobinQ.client;

import java.util.ArrayList;

public interface TableRetrievable {
	public static class RetrieveArgument {
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = (long)(double)_begin;
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = (long)(double)_end;
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + _num;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			RetrieveArgument other = (RetrieveArgument) obj;
			if (_begin == null) {
				if(other._begin != null)
					return false;
			}
			else if(!_begin.equals(other._begin))
				return false;
			if (_end == null) {
				if(other._end != null)
					return false;
			}
			else if(!_end.equals(other._end))
				return false;
			if (_num != other._num)
				return false;
			return true;
		}
		static final int DEFAULT_NUM = 10;
		static final int DEFAULT_MAX = 500;
		Double _begin;
		Double _end;
		int _num;
		public RetrieveArgument(Double beg, Double end, int num) {
			_begin = beg;
			_end = end;
			_num = num;
			
		}
		public RetrieveArgument(int num) {
			this(null, null, num);
		}
		public RetrieveArgument(double beg, double end) {
			this(beg, end, DEFAULT_MAX);
		}
		public RetrieveArgument() {
			this(DEFAULT_NUM);
		}
	};
	public JSONTable retrieve(String url, String tableName, ArrayList<String> fields, RetrieveArgument arg) throws BlockException;
}
