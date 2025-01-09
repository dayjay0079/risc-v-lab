import chisel3._
import chisel3.util._

class MemoryInstruction(fpga: Boolean) extends Module {
  val io = IO(new Bundle {
    val pc = Input(UInt(32.W))
    val instruction = Output(UInt(32.W))
  })

  // Define a method to handle unified memory access
  def readMemory(addr: UInt): UInt = {
    if (fpga) {
      IM.asInstanceOf[SyncReadMem[UInt]].read(addr)
    } else {
      IM.asInstanceOf[Vec[UInt]](addr)
    }
  }

  // Declare IM as AnyRef (parent type for both SyncReadMem and Vec)
  private val IM: AnyRef = if (fpga) {
    SyncReadMem(1024, UInt(32.W)) // Type: SyncReadMem
  } else {
    RegInit(VecInit(Seq.fill(1024)("b00000000010100000000000010010011".U(32.W)))) // Type: Vec
  }
  
  // Example usage of readMemory
  io.instruction := readMemory(io.pc)
}
