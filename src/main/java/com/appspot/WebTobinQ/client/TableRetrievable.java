package com.appspot.WebTobinQ.client;

import java.util.ArrayList;

public interface TableRetrievable {
	public static class RetrieveArgument {
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = (long)_begin;
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = (long)_end;
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result
					+ ((_fieldName == null) ? 0 : _fieldName.hashCode());
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
			if (_begin != other._begin)
				return false;
			if (_end != other._end)
				return false;
			if (_fieldName == null) {
				if (other._fieldName != null)
					return false;
			} else if (!_fieldName.equals(other._fieldName))
				return false;
			if (_num != other._num)
				return false;
			return true;
		}
		static final int DEFAULT_NUM = 10;
		static final int DEFAULT_MAX = 500;
		String _fieldName;
		double _begin;
		double _end;
		int _num;
		public RetrieveArgument(String fieldName, double beg, double end, int num) {
			_fieldName = fieldName;
			_begin = beg;
			_end = end;
			_num = num;
			
		}
		public RetrieveArgument(int num) {
			this(null, 0, 0, num);
		}
		public RetrieveArgument(String fieldName, double beg, double end) {
			this(fieldName, beg, end, DEFAULT_MAX);
		}
		public RetrieveArgument() {
			this(DEFAULT_NUM);
		}
	};
	public JSONTable retrieve(String url, String tableName, ArrayList<String> fields, RetrieveArgument arg) throws BlockException;
}
