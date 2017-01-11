package benplayer

import battlecode.common._
import sun.security.util.Length

class Vector(xc:Float,yc:Float){
    var x: Float = xc;
    var y: Float = yc;
    def this(direction: Direction,length:Float)
        = this(direction.getDeltaX(length),direction.getDeltaY(length))
    def + (other:Vector):Vector = new Vector(x + other.x, y + other.y)
	def scale(dis:Float):Vector = new Vector(x * dis,y * dis)
	def direction() = new Direction(x,y)
	def length() = math.sqrt(x*x + y*y).toFloat
	def unit() = scale(length())
}
class Movement(){
	var lin_vec:Vector = new Vector(0,0)
	def add_liniar_pull(vec:Vector): Unit ={
		lin_vec = lin_vec + vec
	}
	def get_movement() = lin_vec
}
