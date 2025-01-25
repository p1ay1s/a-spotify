package com.niki.util

data class Point(val x: Float, val y: Float)

/**
 * 求直线ab、cd的交点坐标
 */
fun getIntersectionPoint(a: Point, b: Point, c: Point, d: Point): Point? {
    val denominator = (a.x - b.x) * (c.y - d.y) - (a.y - b.y) * (c.x - d.x)

    if (denominator == 0.0F) {
        return null // 直线平行或重合,无交点
    }

    val t = ((a.x - c.x) * (c.y - d.y) - (a.y - c.y) * (c.x - d.x)) / denominator

    val intersectionX = a.x + t * (b.x - a.x)
    val intersectionY = a.y + t * (b.y - a.y)

    return Point(intersectionX, intersectionY)
}