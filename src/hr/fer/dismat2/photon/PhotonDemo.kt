package hr.fer.dismat2.photon

import java.lang.System.exit
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Global calculation precision.
 */
const val PRECISION = 100
/**
 * Global [MathContext] with specified `precision`.
 */
val MC = MathContext(PRECISION)

/**
 * Models a simple point in high precision.
 */
data class Point(var x: BigDecimal, var y: BigDecimal) {

    fun calculateMidpoint(other: Point): Point {
        val two = BigDecimal(2)
        return Point((this.x + other.x) / two, (this.y + other.y) / two)
    }

    fun toShortString(): String {
        val xRound = x.setScale(6, RoundingMode.HALF_UP)
        val yRound = y.setScale(6, RoundingMode.HALF_UP)
        return "($xRound, $yRound)"
    }
}

/**
 * Models a simple line in high precision.
 * Formulated as `A*x + B*y + C = 0`
 */
data class Line(var A: BigDecimal, var B: BigDecimal, var C: BigDecimal) {
    fun getX(y: BigDecimal): BigDecimal? {
        if (A == BigDecimal.ZERO) {
            return null
        }
        return -(B * y + C) / A
    }

    fun getY(x: BigDecimal): BigDecimal? {
        if (B == BigDecimal.ZERO) {
            return null
        }
        return -(A * x + C) / B
    }
}

/**
 * Models a simple circle in high precision.
 */
data class Circle(val center: Point, val radius: BigDecimal = PhotonDemo.R)


@Override
operator fun BigDecimal.plus(other: BigDecimal): BigDecimal = this.add(other, MC)

@Override
operator fun BigDecimal.minus(other: BigDecimal): BigDecimal = this.subtract(other, MC)

@Override
operator fun BigDecimal.unaryMinus(): BigDecimal = this.negate()

@Override
operator fun BigDecimal.times(other: BigDecimal): BigDecimal = this.multiply(other, MC)

@Override
operator fun BigDecimal.div(other: BigDecimal): BigDecimal = this.divide(other, MC)


/**
 * @param position photon's current position. Must be on [trajectory].
 * @param trajectory the trajectory on which the photon is currently moving (between hitting mirrors).
 * @param direction `+1` if the angle of the velocity vector is in range `<-π/2, +π/2]`, `-1` otherwise.
 */
class PhotonDemo(private var position: Point, private var trajectory: Line, private var direction: Int) {

    /**
     * Cumulative travelled distance. Less than [D].
     */
    private var travelledDistance: BigDecimal = BigDecimal.ZERO

    /**
     * Last circle from which the photon was reflected.
     */
    private var lastReflectedCircle: Circle? = null

    companion object {

        /**
         * Circle radius, measured in points.
         */
        val R: BigDecimal = BigDecimal.ONE / BigDecimal(3)

        /**
         * Velocity, measured in points / second.
         */
        private val V: BigDecimal = BigDecimal.ONE

        /**
         * Timeout, measured in seconds.
         */
        private val T: BigDecimal = BigDecimal(20)

        /**
         * Maximum distance, measured in points.
         */
        val D: BigDecimal = V * T

        /**
         * Iteration distance used in ray casting.
         */
        private val ITER_DIST = BigDecimal("0.4")
    }

    private fun distanceFromCenter(line: Line, circle: Circle): BigDecimal {
        val numeratorTmp = line.A * circle.center.x + line.B * circle.center.y + line.C
        val denominatorTmp = line.A * line.A + line.B * line.B
        return numeratorTmp.abs(MC) / denominatorTmp.sqrt(MC)
    }

    private fun distanceBetweenPoints(point1: Point, point2: Point): BigDecimal {
        val dx = point2.x - point1.x
        val dy = point2.y - point1.y
        return (dx * dx + dy * dy).sqrt(MC)
    }

    /**
     * @return `null` if intersection does not exist, [Point] array of size 2 otherwise.
     */
    private fun intersect(line: Line, circle: Circle): Array<Point>? {

        // even if the line only touches the circle, trajectory will not be changed
        if (distanceFromCenter(line, circle) >= R) {
            return null
        }

        val a = line.A
        val b = line.B
        val c = line.C

        val x = circle.center.x
        val y = circle.center.y

        val a2 = a * a
        val b2 = b * b
        val c2 = c * c
        val r2 = R * R

        val two = BigDecimal(2)

        val tmp1 = a2 * r2
        val tmp2 = a2 * x * x
        val tmp3 = a * b * x * y * two
        val tmp4 = a * c * x * two
        val tmp5 = b2 * r2
        val tmp6 = b2 * y * y
        val tmp7 = b * c * y * two
        val tmp8 = b2 * x - a * c - a * b * y
        val tmp9 = a2 + b2
        val tmp10 = a * a * y - b * c - a * b * x

        val tmpUnderRoot = tmp1 - tmp2 - tmp3 - tmp4 + tmp5 - tmp6 - tmp7 - c2  // some serious black magic
        val tmpRoot = tmpUnderRoot.sqrt(MC)

        val x1 = -(b * tmpRoot - tmp8) / tmp9
        val x2 = (b * tmpRoot + tmp8) / tmp9

        val y1 = (a * tmpRoot + tmp10) / tmp9
        val y2 = -(a * tmpRoot - tmp10) / tmp9

        return arrayOf(
                Point(x1, y1),
                Point(x2, y2)
        )
    }

    /**
     * If [trajectory] intersects with [circle], the photon is updated to represent the reflected photon.
     * If this reflection would cover distance larger than [D], the program is terminated.
     * If there is no intersection, nothing happens.
     *
     * @return `true` if reflection occurred, `false` otherwise.
     */
    private fun reflect(circle: Circle): Boolean {

        val twoIntersections: Array<Point> = intersect(trajectory, circle)
                ?: return false

        val dist0 = distanceBetweenPoints(position, twoIntersections[0])
        val dist1 = distanceBetweenPoints(position, twoIntersections[1])

        val intersection: Point = twoIntersections[if (dist0 < dist1) 0 else 1]

        println(intersection.toShortString())

        val wantedDistance = distanceBetweenPoints(position, intersection)

        if (travelledDistance + wantedDistance >= D) {

            val x1 = position.x + (intersection.x - position.x) * (D - travelledDistance) / wantedDistance
            val y1 = trajectory.getY(x1)

            val finalPoint = if (y1 != null) {
                Point(x1, y1)
            } else {
                Point(
                        position.x,
                        position.y + BigDecimal(direction) * wantedDistance
                )
            }

            travelledDistance += distanceBetweenPoints(position, finalPoint)

            println("FINAL POINT: \n$finalPoint")
            println("Covered distance: \n$travelledDistance")
            exit(0)
        }

        val xR = circle.center.x
        val yR = circle.center.y

        val k1 = (intersection.y - yR) / (intersection.x - xR)
        val tmp = BigDecimal(-2) * (k1 * position.x - position.y + yR - k1 * xR) / (k1 * k1 + BigDecimal.ONE)

        val x2 = tmp * k1 + position.x
        val y2 = -tmp + position.y

        val k2 = (y2 - intersection.y) / (x2 - intersection.x)

        travelledDistance += wantedDistance
        position = intersection
        trajectory = Line(-k2, BigDecimal.ONE, k2 * x2 - y2)
        lastReflectedCircle = circle

        val reflectedIntersections = intersect(trajectory, circle)
        val insideCircle = reflectedIntersections!![0].calculateMidpoint(reflectedIntersections[1])

        val directionTemp = if (intersection.x != insideCircle.x) {
            intersection.x > insideCircle.x
        } else {
            intersection.y > insideCircle.y
        }
        direction = if (directionTemp) 1 else -1

        return true
    }

    fun run() {
        println(position.toShortString())

        while (true) {
            val moveX = if (trajectory.B == BigDecimal.ZERO) {
                false
            } else {
                (trajectory.A / trajectory.B).abs(MC) <= BigDecimal.ONE
            }

            val tmpPosition = Point(position.x, position.y)

            inner@ while (true) {
                val x = if (direction > 0) {
                    tmpPosition.x.setScale(0, RoundingMode.CEILING)
                } else {
                    tmpPosition.x.setScale(0, RoundingMode.FLOOR)
                }

                val yFloor = tmpPosition.y.setScale(0, RoundingMode.FLOOR)
                val yCeil = tmpPosition.y.setScale(0, RoundingMode.CEILING)

                val circles = arrayOf(
                        Circle(Point(x, yFloor)),
                        Circle(Point(x, yCeil))
                )

                for (circle in circles) {
                    if (circle == lastReflectedCircle) {
                        continue
                    }
                    if (reflect(circle)) {
                        break@inner
                    }
                }

                if (moveX) {
                    tmpPosition.x += ITER_DIST * BigDecimal(direction)
                    tmpPosition.y = trajectory.getY(tmpPosition.x)!!
                } else {
                    val directionModification = when {
                        trajectory.B == BigDecimal.ZERO -> direction
                        (-trajectory.A / trajectory.B) >= BigDecimal.ZERO -> direction
                        else -> -direction
                    }

                    tmpPosition.y += ITER_DIST * BigDecimal(directionModification)
                    tmpPosition.x = trajectory.getX(tmpPosition.y)!!
                }
            }
        }
    }
}


fun main(args: Array<String>) {
    PhotonDemo(
            Point(BigDecimal("0.50"), BigDecimal("0.26")),          // (0.50, 0.26)
            Line(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal("-0.26")), // y = 0.26
            1                                                       // moving right
    ).run()
}
