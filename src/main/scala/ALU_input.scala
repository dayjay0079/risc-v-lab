import chisel3._
import chisel3.util._
class ALU_input extends Module{
  val io = IO(new Bundle{
    val input = Input(new PipelineValuesEX)
    val data_EX = Input(SInt(32.W))
    val data_MEM = Input(SInt(32.W))
    val data_WB = Input(SInt(32.W))
    val EX_control = Input(UInt(4.W))

    val output = Output(new PipelineValuesEX)
  })

  val output = Wire(new PipelineValuesEX)
  output := io.input

  switch(io.EX_control){
    is(0.U) {
      output.data1 := io.input.data1
      output.data2 := io.input.data2
    }
    is(1.U) {
      output.data2 := io.data_EX
    }
    is(2.U) {
      output.data2 := io.data_MEM
    }
    is(3.U) {
      output.data2 := io.data_WB
    }
    is(4.U) {
      output.data1 := io.data_EX
    }
    is(5.U) {
      output.data1 := io.data_MEM
    }
    is(6.U) {
      output.data1 := io.data_WB
    }
    is(7.U) {
      output.data1 := io.data_EX
      output.data2 := io.data_EX
    }
    is(8.U) {
      output.data1 := io.data_MEM
      output.data2 := io.data_MEM
    }
    is(9.U) {
      output.data1 := io.data_WB
      output.data2 := io.data_WB
    }
    is(10.U) {
      output.data1 := io.data_EX
      output.data2 := io.data_MEM
    }
    is(11.U) {
      output.data1 := io.data_EX
      output.data2 := io.data_WB
    }
    is(12.U) {
      output.data1 := io.data_MEM
      output.data2 := io.data_EX
    }
    is(13.U) {
      output.data1 := io.data_WB
      output.data2 := io.data_EX
    }
    is(14.U) {
      output.data1 := io.data_MEM
      output.data2 := io.data_WB
    }
    is(15.U) {
      output.data1 := io.data_WB
      output.data2 := io.data_MEM
    }
  }

  io.output := output
}
