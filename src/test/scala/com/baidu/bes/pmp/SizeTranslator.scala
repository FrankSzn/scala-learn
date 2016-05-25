package com.baidu.bes.pmp

import com.baidu.bes.pmp.PMPOrderPriceCalc.{TuStat, FileReader}

/**
(960*60,257698038720)
(300*100,429496729900)
(360*90,386547057000)
(320*48,206158430528)
(120*270,1159641170040)
(360*300,1288490189160)
(360*50,214748365160)
(336*280,1202590843216)
(960*90,386547057600)
(640*80,343597384320)
(320*50,214748365120)
(728*90,386547057368)
(160*80,343597383840)
(640*90,386547057280)
(760*90,386547057400)
(1000*90,386547057640)
(400*300,1288490189200)
(672*120,515396076192)
(180*150,644245094580)
(120*600,2576980377720)
(200*60,257698037960)
(580*90,386547057220)
(500*200,858993459700)
(125*125,536870912125)
(360*108,463856468328)
(320*80,343597384000)
(640*60,257698038400)
(480*160,687194767840)
(160*600,2576980377760)
(264*160,687194767624)
(256*58,249108103424)
(300*250,1073741824300)
(468*60,257698038228)
(250*250,1073741824250)
(460*60,257698038220)
(300*50,214748365100)
(120*240,1030792151160)
(240*180,773094113520)
(200*200,858993459400)

  * @author zhangxu
  */
object SizeTranslator extends App {

  val size = FileReader("src/main/scala/com/baidu/bes/pmp/size.txt").fromFile((s: String) => {
    (s, s.getSizeBitMask)
  })

  size.foreach(println(_))

  implicit class get(s: String) {
    def getSizeBitMask = {
      try {
        val widthAndHeight = s.split("\\*")
        widthAndHeight(1).toLong << 32 | widthAndHeight(0).toLong
      } catch {
        case e: Exception => {
          e.printStackTrace()
          0L
        }
      }
    }
  }

}
