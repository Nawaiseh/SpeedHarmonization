package edu.smu.trl.safety.Data;

public class Number3d
{

	private static Number3d _temp = new Number3d();
	public float X;
	public float Y;
	public float Z;


	public Number3d()
	{
		X = 0;
		Y = 0;
		Z = 0;
	}

	public Number3d(float $x, float $y, float $z)
	{
		X = $x;
		Y = $y;
		Z = $z;
	}

	public static Number3d add(Number3d a, Number3d b)
	{
		return new Number3d(a.X + b.X, a.Y + b.Y, a.Z + b.Z);
	}

	public static Number3d subtract(Number3d a, Number3d b)
	{
		return new Number3d(a.X - b.X, a.Y - b.Y, a.Z - b.Z);
	}

	public static Number3d multiply(Number3d a, Number3d b)
	{
		return new Number3d(a.X * b.X, a.Y * b.Y, a.Z * b.Z);
	}

	public static Number3d cross(Number3d v, Number3d w)
	{
		return new Number3d((w.Y * v.Z) - (w.Z * v.Y), (w.Z * v.X) - (w.X * v.Z), (w.X * v.Y) - (w.Y * v.X));
	}

	public static float dot(Number3d v, Number3d w)
	{
		return (v.X * w.X + v.Y * w.Y + w.Z * v.Z);
	}

	public void setAll(float $x, float $y, float $z)
	{
		X = $x;
		Y = $y;
		Z = $z;
	}

	public void setAllFrom(Number3d $n)
	{
		X = $n.X;
		Y = $n.Y;
		Z = $n.Z;
	}

	public void normalize()
	{
		float mod = (float) Math.sqrt(this.X * this.X + this.Y * this.Y + this.Z * this.Z);

		if (mod != 0 && mod != 1)
		{
			mod = 1 / mod;
			this.X *= mod;
			this.Y *= mod;
			this.Z *= mod;
		}
	}

	public void add(Number3d n)
	{
		this.X += n.X;
		this.Y += n.Y;
		this.Z += n.Z;
	}

	public void subtract(Number3d n)
	{
		this.X -= n.X;
		this.Y -= n.Y;
		this.Z -= n.Z;
	}

	public void multiply(float f)
	{
		this.X *= f;
		this.Y *= f;
		this.Z *= f;
	}

	public float length()
	{
		return (float) Math.sqrt(this.X * this.X + this.Y * this.Y + this.Z * this.Z);
	}

	//

	public Number3d clone()
	{
		return new Number3d(X, Y, Z);
	}

	public void rotateX(float angle)
	{
		float cosRY = (float) Math.cos(angle);
		float sinRY = (float) Math.sin(angle);

		_temp.setAll(this.X, this.Y, this.Z);

		this.Y = (_temp.Y * cosRY) - (_temp.Z * sinRY);
		this.Z = (_temp.Y * sinRY) + (_temp.Z * cosRY);
	}

	public void rotateY(float angle)
	{
		float cosRY = (float) Math.cos(angle);
		float sinRY = (float) Math.sin(angle);

		_temp.setAll(this.X, this.Y, this.Z);

		this.X = (_temp.X * cosRY) + (_temp.Z * sinRY);
		this.Z = (_temp.X * -sinRY) + (_temp.Z * cosRY);
	}

	public void rotateZ(float angle)
	{
		float cosRY = (float) Math.cos(angle);
		float sinRY = (float) Math.sin(angle);

		_temp.setAll(this.X, this.Y, this.Z);

		this.X = (_temp.X * cosRY) - (_temp.Y * sinRY);
		this.Y = (_temp.X * sinRY) + (_temp.Y * cosRY);
	}

	private void Add(Number3d Point)
	{
		X += Point.X;
		Y += Point.Y;
	}

	private void Subtract(Number3d Point)
	{
		X -= Point.X;
		Y -= Point.Y;
	}

	public void RotateAroundPoint(Number3d PivotPoint, float Theta)
	{
		float Sin = (float) Math.sin(Theta);
		float Cos = (float) Math.cos(Theta);
		Subtract(PivotPoint);

		float _X = X * Cos - Y * Sin;
		float _Y = X * Sin + Y * Cos;

		X = _X;
		Y = _Y;

		Add(PivotPoint);
	}
	@Override
	public String toString()
	{
		return X + "," + Y + "," + Z;
	}

}
