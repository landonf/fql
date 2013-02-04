package fql;


public class Arr<Obj,Arrow> {

		@Override
	public String toString() {
		return "(" + arr + " : " + src + " -> " + dst + ")";
	}
		Arrow arr;
		Obj src;
		Obj dst;
		
		public Arr(Arrow arr, Obj src, Obj dst) {
			super();
			this.arr = arr;
			this.src = src;
			this.dst = dst;
		}
		@Override
		public int hashCode() {
			return 0;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Arr other = (Arr) obj;
			if (arr == null) {
				if (other.arr != null)
					return false;
			} else if (!arr.equals(other.arr))
				return false;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			return true;
		}

	
}
